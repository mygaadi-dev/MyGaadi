package com.mygaadi.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import lombok.RequiredArgsConstructor;

@Configuration // declares Java configuration class - to declare spring beans
@EnableWebSecurity // to enable spring web security
@EnableMethodSecurity // to enable method level authorization rules 
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final CustomJwtVerificationFilter jwtFilter;

    @Value("${app.cors-origins}")
    private String corsOrigins;

    /*
     * Configure spring sec filter chain 
     *  - from spring bean HttpSecurity 
     *   - Builder class , to customize filter chain
     */
    @Bean
    public SecurityFilterChain customizeSecurityFilterChain(HttpSecurity http) throws Exception {
        //1. disable CSRF protection
        http.csrf(csrf -> csrf.disable());
        
        //1.1 Configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //2. Disable HttpSession creation (Spring security will NOT create HttpSession
        // to store security context info.)
        http.sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //4. Define URL based authorization rules
        http.authorizeHttpRequests(request -> request
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/auth/**", "/api/kyc/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/actuator/health", "/uploads/**", "/demo-cars/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/cars/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/reviews/seller/**").permitAll()
            .anyRequest().authenticated()
        );

        //add custom jwt verification filter before - UsernamePasswordAuthFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build(); //rets the object of DefaultSecurityFilter -> implements -> SecurityFilterChain
    }

    /*
     * Configure Password encoder bean
     * - BCryptPasswordEncoder
     *  - SHA with salt
     *  - public boolean matches(String raw,String encPwd)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * Configure AuthenticationManager
     *  - as spring bean
     *  - using AuthConfig
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
