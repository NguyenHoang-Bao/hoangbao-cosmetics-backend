package com.hoangbaoshop.hoangbaocosmetics_backend.service;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    // đăng ký tài khoản mới
    void register(RegisterRequest request);

    // đăng nhập bằng username / password
    AuthResponse login(LoginRequest request);

    // đăng nhập bằng Google OAuth
    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    // đăng nhập bằng Facebook OAuth
    AuthResponse loginWithFacebook(FacebookLoginRequest request);
}
