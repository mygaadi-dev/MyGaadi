package com.mygaadi.dto.auth;

import com.mygaadi.dto.user.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresInSeconds;
    private UserResponse user;
}
