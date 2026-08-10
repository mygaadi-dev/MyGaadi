package com.mygaadi.payment.security;

import com.mygaadi.payment.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPrincipal {
    private Long userId;
    private String email;
    private UserRole role;
}
