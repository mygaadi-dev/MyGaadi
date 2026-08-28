package com.mygaadi.security;

import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.mygaadi.model.entity.User;
import com.mygaadi.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJwtVerificationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 1. Check if Authorization header exists in the incoming request & starts with Bearer
            String headerValue = request.getHeader("Authorization");
            if (headerValue != null && headerValue.startsWith("Bearer ")) {
                // => 2. Extract JWT & validate it using JwtUtils & get claims
                String jwt = headerValue.substring(7);
                Claims claims = jwtUtils.verifyJwt(jwt);
                
                String email = claims.getSubject();
                
                // => 3. Load User from DB to keep the principal as a User entity for CurrentUser.get()
                User user = userRepository.findByEmail(email.toLowerCase()).orElse(null);
                if (user != null) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(authority)
                    );
                    /*
                     * 4. Add authentication object under - Spring security context holder - so that
                     * next filters can get auth details directly
                     */
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                log.info("******NO JWT *********");
            }
            // in case of no exceptions -> continue to the next Filter | D.S
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            /* -> invalid jwt -> abort further request processing
             * clear sec ctx holder
             * send error resp (SC 401) to the client 
             */
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Invalid JWT - Authentication Failed!!!!!");
            return;
        }
    }
}
