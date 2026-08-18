package com.mygaadi.dto.user;

import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.Role;
import com.mygaadi.model.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private String profilePicUrl;
    private boolean emailVerified;
    private String panNumber;
    private String kycHolderName;
    private String kycStatus;
    private boolean kycVerified;
    private Instant createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .profilePicUrl(user.getProfilePicUrl())
                .emailVerified(user.isEmailVerified())
                .panNumber(user.getPanNumber())
                .kycHolderName(user.getKycHolderName())
                .kycStatus(user.getKycStatus())
                .kycVerified(user.isKycVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
