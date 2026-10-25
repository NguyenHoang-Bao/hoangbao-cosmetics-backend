package com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt;

import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterFailSafeTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WhenRedisThrowsConnectionException_FailsOpenAndAuthenticatesUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt");
        when(jwtUtils.validateJwtToken("valid-jwt")).thenReturn(true);
        // Simulate Redis outage
        when(redisTemplate.hasKey("blacklist:jwt:valid-jwt"))
                .thenThrow(new RedisConnectionFailureException("Cannot connect to Redis server: Connection refused"));

        when(jwtUtils.getUserNameFromJwtToken("valid-jwt")).thenReturn("valid_customer");

        UserDetails userDetails = mock(UserDetails.class);
        doReturn(Collections.emptyList()).when(userDetails).getAuthorities();
        when(userDetailsService.loadUserByUsername("valid_customer")).thenReturn(userDetails);

        // When
        authTokenFilter.doFilter(request, response, filterChain);

        // Then: User authentication is maintained even though Redis is down!
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
