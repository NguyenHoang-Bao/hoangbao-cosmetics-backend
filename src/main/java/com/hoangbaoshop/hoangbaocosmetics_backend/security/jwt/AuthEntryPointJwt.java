package com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Truy cập chưa xác thực: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = authException.getMessage() != null
                ? authException.getMessage().replace("\"", "\\\"")
                : "Truy cập bị từ chối hoặc token không hợp lệ";

        String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"Lỗi xác thực: %s\",\"data\":null}",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}
