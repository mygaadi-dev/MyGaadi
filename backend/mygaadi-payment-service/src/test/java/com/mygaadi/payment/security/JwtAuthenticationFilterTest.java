package com.mygaadi.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygaadi.payment.enums.UserRole;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidBearerToken() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parse("valid-token")).thenReturn(JwtPrincipal.builder()
                .userId(1L).email("buyer@mygaadi.com").role(UserRole.BUYER).build());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ROLE_BUYER", SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parse("bad-token")).thenThrow(new IllegalArgumentException("bad"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or expired access token"));
        verifyNoInteractions(chain);
    }
}
