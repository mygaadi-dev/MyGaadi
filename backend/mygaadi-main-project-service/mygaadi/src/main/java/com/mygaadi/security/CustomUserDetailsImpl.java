package com.mygaadi.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.mygaadi.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserDetailsImpl implements UserDetails {
    private final Long userId;
    private final String email;
    private final String password;
    private final Role role;
    private final String completeName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // In our database Role is BUYER, SELLER, ADMIN.
        // We prepend ROLE_ to follow Spring Security's role convention.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
