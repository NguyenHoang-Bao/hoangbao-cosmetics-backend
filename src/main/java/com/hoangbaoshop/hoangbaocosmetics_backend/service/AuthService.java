package com.hoangbaoshop.hoangbaocosmetics_backend.service;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
public interface AuthService {
    //logic dang ky tai khoan
    void register(RegisterRequest request);

    //logic dang nhap
    AuthResponse login(LoginRequest request);
}
