package com.mygaadi.controller;

import com.mygaadi.common.ApiResponse;
import com.mygaadi.dto.auth.*;
import com.mygaadi.dto.user.UserResponse;
import com.mygaadi.security.CurrentUser;
import com.mygaadi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/register")
    @Operation(summary = "Register buyer or seller")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Legacy login endpoint; OTP is required for all roles")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("OTP verification required", authService.login(request));
    }

    @PostMapping("/buyer/login/start")
    @Operation(summary = "Buyer login step 1: password verification and email OTP")
    public ApiResponse<Map<String, Object>> buyerLoginStart(@Valid @RequestBody OtpLoginStartRequest request) {
        return ApiResponse.ok("Buyer login OTP sent", authService.startBuyerOtpLogin(request));
    }

    @PostMapping("/buyer/login/verify")
    @Operation(summary = "Buyer login step 2: OTP verification")
    public ApiResponse<AuthResponse> buyerLoginVerify(@Valid @RequestBody OtpLoginVerifyRequest request) {
        return ApiResponse.ok("Buyer logged in successfully", authService.verifyBuyerOtpLogin(request));
    }

    @PostMapping("/seller/login/start")
    @Operation(summary = "Seller/Agent login step 1: password verification and OTP send")
    public ApiResponse<Map<String, Object>> sellerLoginStart(@Valid @RequestBody OtpLoginStartRequest request) {
        return ApiResponse.ok("Seller login OTP sent", authService.startSellerOtpLogin(request));
    }

    @PostMapping("/seller/login/verify")
    @Operation(summary = "Seller/Agent login step 2: OTP verification")
    public ApiResponse<AuthResponse> sellerLoginVerify(@Valid @RequestBody OtpLoginVerifyRequest request) {
        return ApiResponse.ok("Seller logged in successfully", authService.verifySellerOtpLogin(request));
    }

    @PostMapping("/admin/login/start")
    @Operation(summary = "Admin login step 1: password verification and OTP send")
    public ApiResponse<Map<String, Object>> adminLoginStart(@Valid @RequestBody OtpLoginStartRequest request) {
        return ApiResponse.ok("Admin login OTP sent", authService.startAdminOtpLogin(request));
    }

    @PostMapping("/admin/login/verify")
    @Operation(summary = "Admin login step 2: OTP verification")
    public ApiResponse<AuthResponse> adminLoginVerify(@Valid @RequestBody OtpLoginVerifyRequest request) {
        return ApiResponse.ok("Admin logged in successfully", authService.verifyAdminOtpLogin(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new access token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/forgot-password/start")
    @Operation(summary = "Step 1: Request password reset OTP")
    public ApiResponse<Map<String, Object>> forgotPasswordStart(@Valid @RequestBody ForgotPasswordStartRequest request) {
        return ApiResponse.ok("Reset OTP sent successfully", authService.startForgotPassword(request));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Step 2: Verify OTP and save new password")
    public ApiResponse<String> forgotPasswordReset(@Valid @RequestBody ForgotPasswordResetRequest request) {
        return ApiResponse.ok("Password updated successfully", authService.resetPassword(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.ok("Logged out", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok("Current user", UserResponse.from(currentUser.get()));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK");
    }
}
