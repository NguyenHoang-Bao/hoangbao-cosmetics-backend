package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.email.OrderEmailDto;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;

public interface EmailService {

    // gửi mã xác thực OTP qua email
    void sendOtpEmail(String toEmail, String otpCode, OtpTokenType tokenType);

    // gửi email xác thực tài khoản 1-click khi đăng ký mới
    void sendAccountVerificationEmail(String toEmail, String fullName, String verificationUrl);

    // gửi email thông báo xác nhận đặt hàng thành công (dùng snapshot DTO)
    void sendOrderConfirmationEmail(OrderEmailDto order, String toEmail);
}
