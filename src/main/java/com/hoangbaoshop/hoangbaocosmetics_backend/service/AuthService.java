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

    // gửi mã OTP đặt lại mật khẩu qua email
    void sendForgotPasswordOtp(com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.SendOtpRequest request);

    // xác thực mã OTP đặt lại mật khẩu
    void verifyForgotPasswordOtp(com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.VerifyOtpRequest request);

    // đặt lại mật khẩu mới bằng mã OTP
    void resetPasswordWithOtp(com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.ResetPasswordWithOtpRequest request);

    // xác thực tài khoản qua liên kết 1-click gửi trong email
    void verifyEmailToken(String token);

    // đăng xuất tài khoản và đưa token vào Redis blacklist
    void logout(String token);
}
