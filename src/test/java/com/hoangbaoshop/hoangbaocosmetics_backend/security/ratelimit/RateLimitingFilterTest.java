package com.hoangbaoshop.hoangbaocosmetics_backend.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        ReflectionTestUtils.setField(rateLimitingFilter, "authLimit", 5);
        ReflectionTestUtils.setField(rateLimitingFilter, "orderLookupLimit", 5);
    }

    @Test
    void testAuthEndpoint_WhenWithinLimit_PassesThrough() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setRemoteAddr("192.168.1.100");
            MockHttpServletResponse response = new MockHttpServletResponse();

            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void testAuthEndpoint_WhenExceedsLimit_ReturnsHttp429() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        // First 5 requests pass
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // 6th request is blocked with HTTP 429
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("POST", "/api/auth/login");
        blockedRequest.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitingFilter.doFilterInternal(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
        assertEquals("60", blockedResponse.getHeader("Retry-After"));
        verify(filterChain, times(5)).doFilter(any(), any()); // blocked request did not call doFilter
    }

    @Test
    void testOrderLookup_WhenExceedsLimit_ReturnsHttp429() throws ServletException, IOException {
        FilterChain filterChain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/ORD-20261010-ABCD12");
            request.setRemoteAddr("10.0.0.2");
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("GET", "/api/orders/ORD-20261010-ABCD12");
        blockedRequest.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitingFilter.doFilterInternal(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
    }
}
