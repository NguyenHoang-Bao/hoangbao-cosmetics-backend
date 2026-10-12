package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordWithOtpRequest {

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Định dạng email không hợp lệ!")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống!")
    @Size(min = 6, max = 6, message = "Mã OTP phải đúng 6 chữ số!")
    private String otpCode;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự!")
    private String newPassword;
}
