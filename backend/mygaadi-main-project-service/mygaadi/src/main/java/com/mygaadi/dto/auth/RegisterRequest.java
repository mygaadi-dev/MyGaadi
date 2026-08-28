package com.mygaadi.dto.auth;

import com.mygaadi.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotNull private Role role;

    // Required only for SELLER/agent registration when PAN KYC is completed through dummy .NET helper service.
    private String panNumber;
    private Boolean kycVerified;
}

