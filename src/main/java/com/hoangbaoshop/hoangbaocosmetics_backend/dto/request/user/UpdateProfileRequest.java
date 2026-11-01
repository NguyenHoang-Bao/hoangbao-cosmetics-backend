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
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 255, message = "Họ và tên không được vượt quá 255 ký tự")
    private String fullName;

    @Pattern(
        regexp = "^$|^(0[3|5|7|8|9])[0-9]{8}$",
        message = "Số điện thoại không đúng định dạng (cần 10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09)"
    )
    private String phoneNumber;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    private String avatar;
}
