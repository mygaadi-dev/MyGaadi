package com.mygaadi.security;

import com.mygaadi.common.ApiException;
import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account is blocked");
        }
        return user;
    }
}
