package com.mygaadi.service;

import com.mygaadi.common.ApiException;
import com.mygaadi.dto.auth.AuthResponse;
import com.mygaadi.dto.auth.LoginRequest;
import com.mygaadi.dto.auth.OtpLoginStartRequest;
import com.mygaadi.dto.auth.OtpLoginVerifyRequest;
import com.mygaadi.dto.auth.ForgotPasswordResetRequest;
import com.mygaadi.dto.auth.ForgotPasswordStartRequest;
import com.mygaadi.dto.auth.RefreshTokenRequest;
import com.mygaadi.dto.auth.RegisterRequest;
import com.mygaadi.dto.kyc.KycVerifiedUser;
import com.mygaadi.dto.user.UserResponse;
import com.mygaadi.model.entity.BuyerProfile;
import com.mygaadi.model.entity.LoginOtp;
import com.mygaadi.model.entity.RefreshToken;
import com.mygaadi.model.entity.SellerProfile;
import com.mygaadi.model.entity.SellerVerification;
import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.DocumentType;
import com.mygaadi.model.enums.NotificationType;
import com.mygaadi.model.enums.Role;
import com.mygaadi.model.enums.UserStatus;
import com.mygaadi.model.enums.VerificationStatus;
import com.mygaadi.repository.BuyerProfileRepository;
import com.mygaadi.repository.LoginOtpRepository;
import com.mygaadi.repository.RefreshTokenRepository;
import com.mygaadi.repository.SellerProfileRepository;
import com.mygaadi.repository.SellerVerificationRepository;
import com.mygaadi.repository.UserRepository;
import com.mygaadi.security.JwtProperties;
import com.mygaadi.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final KycVerificationCacheService kycVerificationCacheService;
    private final NotificationService notificationService;
    private final BuyerProfileRepository buyerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final SellerVerificationRepository sellerVerificationRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.auth.otp-expiry-seconds:300}")
    private long otpExpirySeconds;

    @Value("${app.auth.otp-resend-seconds:60}")
    private long otpResendSeconds;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("Administrator registration is not available through the public application.");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw ApiException.badRequest("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw ApiException.badRequest("Phone already registered");
        }
        KycVerifiedUser verifiedPan = null;
        if (request.getRole() == Role.SELLER) {
            if (request.getPanNumber() == null || request.getPanNumber().isBlank()) {
                throw ApiException.badRequest("PAN verification is required for seller registration");
            }
            verifiedPan = kycVerificationCacheService.getVerifiedPan(request.getPanNumber())
                    .orElseThrow(() -> ApiException.badRequest("Please verify PAN OTP before seller registration"));
        }

        User.UserBuilder userBuilder = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .emailVerified(false);

        if (verifiedPan != null) {
            userBuilder
                    .panNumber(verifiedPan.getPanNumber())
                    .kycHolderName(verifiedPan.getHolderName())
                    .kycStatus(verifiedPan.getKycStatus())
                    .kycVerified(true);
        }

        User user = userRepository.save(userBuilder.build());
        createRoleProfile(user, verifiedPan);

        if (verifiedPan != null) {
            kycVerificationCacheService.consumePan(verifiedPan.getPanNumber());
            notificationService.create(user, NotificationType.SYSTEM, "PAN KYC verified",
                    "Your PAN verification was completed successfully during registration.",
                    user.getId(), "USER");
        } else {
            notificationService.create(user, NotificationType.SYSTEM, "Welcome to MyGaadi.com",
                    "Your buyer account is ready.", user.getId(), "USER");
        }

        return authResponse(user, createRefreshToken(user, "register"));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        throw ApiException.unauthorized("OTP verification is required. Use the role-specific OTP login flow.");
    }

    @Transactional
    public Map<String, Object> startBuyerOtpLogin(OtpLoginStartRequest request) {
        return startOtpLogin(request.getEmail(), request.getPassword(), request.getDeviceInfo(), Role.BUYER);
    }

    @Transactional
    public AuthResponse verifyBuyerOtpLogin(OtpLoginVerifyRequest request) {
        return verifyOtpLogin(request.getEmail(), request.getOtp(), request.getDeviceInfo(), Role.BUYER);
    }

    @Transactional
    public Map<String, Object> startSellerOtpLogin(OtpLoginStartRequest request) {
        return startOtpLogin(request.getEmail(), request.getPassword(), request.getDeviceInfo(), Role.SELLER);
    }

    @Transactional
    public AuthResponse verifySellerOtpLogin(OtpLoginVerifyRequest request) {
        return verifyOtpLogin(request.getEmail(), request.getOtp(), request.getDeviceInfo(), Role.SELLER);
    }

    @Transactional
    public Map<String, Object> startAdminOtpLogin(OtpLoginStartRequest request) {
        return startOtpLogin(request.getEmail(), request.getPassword(), request.getDeviceInfo(), Role.ADMIN);
    }

    @Transactional
    public AuthResponse verifyAdminOtpLogin(OtpLoginVerifyRequest request) {
        return verifyOtpLogin(request.getEmail(), request.getOtp(), request.getDeviceInfo(), Role.ADMIN);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));
        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw ApiException.unauthorized("Refresh token expired");
        }
        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        refreshTokenRepository.flush();
        return authResponse(user, createRefreshToken(user, request.getDeviceInfo()));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Transactional
    public Map<String, Object> startForgotPassword(ForgotPasswordStartRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> ApiException.badRequest("No account found registered with this email address"));

        if (user.isDeleted() || user.getStatus() == UserStatus.BLOCKED) {
            throw ApiException.badRequest("Account is suspended or inactive");
        }

        Instant now = Instant.now();
        loginOtpRepository.findTopByUserAndRoleAndUsedFalseOrderByCreatedAtDesc(user, user.getRole())
                .ifPresent(existing -> {
                    long elapsed = Duration.between(existing.getCreatedAt(), now).getSeconds();
                    if (elapsed < otpResendSeconds && existing.getExpiresAt().isAfter(now)) {
                        long remaining = otpResendSeconds - Math.max(elapsed, 0);
                        throw ApiException.badRequest("Please wait " + remaining + " seconds before requesting another OTP");
                    }
                    existing.setUsed(true);
                });

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        LoginOtp resetOtp = LoginOtp.builder()
                .user(user)
                .role(user.getRole())
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(now.plusSeconds(otpExpirySeconds))
                .deviceInfo("FORGOT_PASSWORD_RESET")
                .build();

        loginOtpRepository.save(resetOtp);
        emailService.sendLoginOtp(user.getEmail(), user.getName(), otp, Math.toIntExact(otpExpirySeconds / 60));

        return Map.of(
                "success", true,
                "code", "RESET_OTP_SENT",
                "message", "Password reset OTP sent to your registered email address",
                "maskedEmail", maskEmail(user.getEmail()),
                "expiresIn", otpExpirySeconds,
                "demoOtp", otp
        );
    }

    @Transactional
    public String resetPassword(ForgotPasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> ApiException.badRequest("Invalid email or reset OTP"));

        LoginOtp loginOtp = loginOtpRepository.findTopByUserAndRoleAndUsedFalseOrderByCreatedAtDesc(user, user.getRole())
                .orElseThrow(() -> ApiException.badRequest("OTP expired or not requested"));

        if (loginOtp.getExpiresAt().isBefore(Instant.now())) {
            loginOtp.setUsed(true);
            throw ApiException.badRequest("OTP expired. Please request a new reset OTP");
        }

        if (!passwordEncoder.matches(request.getOtp(), loginOtp.getOtpHash())) {
            throw ApiException.badRequest("Invalid OTP code");
        }

        loginOtp.setUsed(true);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        notificationService.create(user, NotificationType.SYSTEM, "Password Reset Successful",
                "Your MyGaadi account password was updated successfully.",
                user.getId(), "USER");

        return "Password reset successfully. You can now login with your new password.";
    }

    private Map<String, Object> startOtpLogin(String email, String password, String deviceInfo, Role expectedRole) {
        User user = findActiveByEmailAndPassword(email, password);
        if (user.getRole() != expectedRole) {
            throw ApiException.unauthorized("Invalid login portal for this account");
        }

        Instant now = Instant.now();
        loginOtpRepository.findTopByUserAndRoleAndUsedFalseOrderByCreatedAtDesc(user, expectedRole)
                .ifPresent(existing -> {
                    long elapsed = Duration.between(existing.getCreatedAt(), now).getSeconds();
                    if (elapsed < otpResendSeconds && existing.getExpiresAt().isAfter(now)) {
                        long remaining = otpResendSeconds - Math.max(elapsed, 0);
                        throw ApiException.badRequest("Please wait " + remaining + " seconds before requesting another OTP");
                    }
                    existing.setUsed(true);
                });

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        LoginOtp loginOtp = LoginOtp.builder()
                .user(user)
                .role(expectedRole)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(now.plusSeconds(otpExpirySeconds))
                .deviceInfo(deviceInfo)
                .build();

        loginOtpRepository.save(loginOtp);
        emailService.sendLoginOtp(user.getEmail(), user.getName(), otp, Math.toIntExact(otpExpirySeconds / 60));

        return Map.of(
                "success", true,
                "code", "OTP_SENT",
                "message", "Verification code sent to your registered email address",
                "maskedEmail", maskEmail(user.getEmail()),
                "expiresIn", otpExpirySeconds
        );
    }

    private AuthResponse verifyOtpLogin(String email, String otp, String deviceInfo, Role expectedRole) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or OTP"));
        if (user.getRole() != expectedRole) throw ApiException.unauthorized("Invalid login portal for this account");
        LoginOtp loginOtp = loginOtpRepository.findTopByUserAndRoleAndUsedFalseOrderByCreatedAtDesc(user, expectedRole)
                .orElseThrow(() -> ApiException.unauthorized("OTP expired or not requested"));
        if (loginOtp.getExpiresAt().isBefore(Instant.now())) {
            loginOtp.setUsed(true);
            throw ApiException.unauthorized("OTP expired");
        }
        if (!passwordEncoder.matches(otp, loginOtp.getOtpHash())) {
            throw ApiException.unauthorized("Invalid OTP");
        }
        loginOtp.setUsed(true);
        user.setEmailVerified(true);
        notificationService.create(user, NotificationType.SYSTEM, "Secure login", "Your " + expectedRole.name() + " account was logged in using OTP verification.", user.getId(), "USER");
        return authResponse(user, createRefreshToken(user, deviceInfo));
    }

    private User findActiveByEmailAndPassword(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (user.isDeleted()) throw ApiException.unauthorized("Invalid email or password");
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw ApiException.forbidden("Your account is blocked");
        }
        return user;
    }

    private void createRoleProfile(User user, KycVerifiedUser verifiedPan) {
        if (user.getRole() == Role.BUYER) {
            buyerProfileRepository.save(BuyerProfile.builder().user(user).build());
        } else if (user.getRole() == Role.SELLER) {
            sellerProfileRepository.save(SellerProfile.builder()
                    .user(user)
                    .panNumber(verifiedPan == null ? user.getPanNumber() : verifiedPan.getPanNumber())
                    .kycHolderName(verifiedPan == null ? user.getKycHolderName() : verifiedPan.getHolderName())
                    .kycVerified(true)
                    .build());

            sellerVerificationRepository.findByUser(user).orElseGet(() -> sellerVerificationRepository.save(
                    SellerVerification.builder()
                            .user(user)
                            .documentType(DocumentType.PAN)
                            .documentNumber(user.getPanNumber() != null ? user.getPanNumber() : "PAN_VERIFIED")
                            .documentUrl("KYC_VERIFIED")
                            .status(VerificationStatus.APPROVED)
                            .submittedAt(Instant.now())
                            .reviewedAt(Instant.now())
                            .deleted(false)
                            .build()
            ));
        }
    }

    private AuthResponse authResponse(User user, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(jwtUtils.generateAccessToken(user))
                .refreshToken(refreshToken)
                .accessTokenExpiresInSeconds(jwtProperties.getAccessTokenMinutes() * 60)
                .user(UserResponse.from(user))
                .build();
    }

    private String createRefreshToken(User user, String deviceInfo) {
        String token = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .deviceInfo(deviceInfo)
                .expiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenDays() * 24 * 60 * 60))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}
