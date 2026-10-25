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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

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
    void testDoFilterInternal_WhenUserEnabled_SetsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt");
        when(jwtUtils.validateJwtToken("valid-jwt")).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:valid-jwt")).thenReturn(false);
        when(jwtUtils.getUserNameFromJwtToken("valid-jwt")).thenReturn("active_user");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isEnabled()).thenReturn(true);
        doReturn(Collections.emptyList()).when(userDetails).getAuthorities();
        when(userDetailsService.loadUserByUsername("active_user")).thenReturn(userDetails);

        authTokenFilter.doFilter(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WhenUserDisabled_Returns403Forbidden() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt");
        when(jwtUtils.validateJwtToken("valid-jwt")).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:valid-jwt")).thenReturn(false);
        when(jwtUtils.getUserNameFromJwtToken("valid-jwt")).thenReturn("banned_user");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isEnabled()).thenReturn(false);
        when(userDetailsService.loadUserByUsername("banned_user")).thenReturn(userDetails);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authTokenFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(stringWriter.toString().contains("Tài khoản đã bị khóa hoặc ngừng hoạt động"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WhenTokenBlacklisted_Returns401Unauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer logged-out-jwt");
        when(jwtUtils.validateJwtToken("logged-out-jwt")).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:jwt:logged-out-jwt")).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        authTokenFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Token đã bị vô hiệu hóa sau khi đăng xuất"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }
}
