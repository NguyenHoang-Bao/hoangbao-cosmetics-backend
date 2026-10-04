package com.hoangbaoshop.hoangbaocosmetics_backend.service;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    //logic dang ky tai khoan
    void register(RegisterRequest request);

    //logic dang nhap
    AuthResponse login(LoginRequest request);

    //logic dang nhap Google OAuth
    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    // Logic đăng nhập Facebook OAuth
    AuthResponse loginWithFacebook(FacebookLoginRequest request);
}
