package com.hoangbaoshop.hoangbaocosmetics_backend.security;

import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.AuthEntryPointJwt;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.AuthTokenFilter;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.ratelimit.RateLimitingFilter;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class SecurityConfigFilterRegistrationTest {

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthEntryPointJwt unauthorizedHandler;

    @Mock
    private RateLimitingFilter rateLimitingFilter;

    @Test
    @DisplayName("rateLimitingFilterRegistration - Should disable automatic servlet container registration")
    void shouldDisableRateLimitingFilterServletRegistration() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService, unauthorizedHandler, rateLimitingFilter);

        FilterRegistrationBean<RateLimitingFilter> registration = securityConfig.rateLimitingFilterRegistration(rateLimitingFilter);

        assertNotNull(registration);
        assertSame(rateLimitingFilter, registration.getFilter());
        assertFalse(registration.isEnabled(), "FilterRegistrationBean must have enabled=false so it only runs in SecurityFilterChain");
    }

    @Test
    @DisplayName("authTokenFilterRegistration - Should disable automatic servlet container registration")
    void shouldDisableAuthTokenFilterServletRegistration() {
        SecurityConfig securityConfig = new SecurityConfig(userDetailsService, unauthorizedHandler, rateLimitingFilter);
        AuthTokenFilter authTokenFilter = new AuthTokenFilter();

        FilterRegistrationBean<AuthTokenFilter> registration = securityConfig.authTokenFilterRegistration(authTokenFilter);

        assertNotNull(registration);
        assertSame(authTokenFilter, registration.getFilter());
        assertFalse(registration.isEnabled(), "FilterRegistrationBean must have enabled=false so it only runs in SecurityFilterChain");
    }
}
