package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStaffRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Tên đăng nhập chỉ chứa chữ cái, số, dấu chấm, gạch dưới hoặc gạch nối")
    private String username;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ (10 chữ số)")
    private String phoneNumber;

    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Vai trò nhân sự không được để trống")
    private String roleName;
}
