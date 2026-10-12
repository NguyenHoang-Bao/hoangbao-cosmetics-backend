package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;

public interface OtpService {

    // sinh mã OTP ngẫu nhiên, lưu vào db và gửi email cho người dùng
    void generateAndSendOtp(String email, OtpTokenType tokenType);

    // kiểm tra tính hợp lệ của mã OTP
    void verifyOtp(String email, String otpCode, OtpTokenType tokenType);

    // hoàn tất việc sử dụng OTP (đánh dấu isUsed = true)
    void consumeOtp(String email, String otpCode, OtpTokenType tokenType);
}
