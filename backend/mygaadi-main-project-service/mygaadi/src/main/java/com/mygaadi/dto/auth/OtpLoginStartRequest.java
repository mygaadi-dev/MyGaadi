package com.mygaadi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpLoginStartRequest {
    @Email @NotBlank private String email;
    @NotBlank private String password;
    private String deviceInfo;
}
