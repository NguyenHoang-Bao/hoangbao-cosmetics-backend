package com.hoangbaoshop.hoangbaocosmetics_backend.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filter giới hạn tần suất gọi API (Rate Limiting) trong bộ nhớ:
 * - Bảo vệ /api/auth/** chống tấn công brute-force / credential stuffing.
 * - Bảo vệ /api/orders/* (GET) chống vét cạn (enumeration) mã đơn hàng.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.auth-limit:30}")
    private int authLimit;

    @Value("${app.rate-limit.order-lookup-limit:40}")
    private int orderLookupLimit;

    private static final long WINDOW_DURATION_MS = 60_000L; // Cửa sổ trượt 1 phút (60 giây)

    private final Map<String, RequestCounter> authRateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> orderLookupRateLimitMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static class RequestCounter {
        final long windowStartTime;
        final AtomicInteger count;

        RequestCounter(long windowStartTime) {
            this.windowStartTime = windowStartTime;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIp(request);

        // 1. Giới hạn tần suất cho các API xác thực /api/auth/**
        if (path.startsWith("/api/auth")) {
            if (!isAllowed(authRateLimitMap, clientIp, authLimit)) {
                log.warn("Rate limit vượt ngưỡng cho API Auth từ IP: {} (Path: {})", clientIp, path);
                sendTooManyRequestsResponse(response, "Bạn đã gửi quá nhiều yêu cầu xác thực. Vui lòng thử lại sau 1 phút!");
                return;
            }
        }

        // 2. Giới hạn tần suất cho GET /api/orders/* (tra cứu đơn hàng công khai, loại trừ /my-orders)
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/orders/") && !path.equals("/api/orders/my-orders")) {
            if (!isAllowed(orderLookupRateLimitMap, clientIp, orderLookupLimit)) {
                log.warn("Rate limit vượt ngưỡng cho tra cứu đơn hàng từ IP: {} (Path: {})", clientIp, path);
                sendTooManyRequestsResponse(response, "Bạn đã gửi quá nhiều yêu cầu tra cứu đơn hàng. Vui lòng thử lại sau 1 phút!");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(Map<String, RequestCounter> map, String clientIp, int maxLimit) {
        long now = System.currentTimeMillis();

        // Định kỳ dọn dẹp khi bộ nhớ phình to để tránh rò rỉ RAM
        if (map.size() > 2000) {
            map.entrySet().removeIf(entry -> (now - entry.getValue().windowStartTime) > (5 * WINDOW_DURATION_MS));
        }

        RequestCounter counter = map.compute(clientIp, (key, existing) -> {
            if (existing == null || (now - existing.windowStartTime) > WINDOW_DURATION_MS) {
                return new RequestCounter(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return counter.count.get() <= maxLimit;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429); // 429 Too Many Requests
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "60");

        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
