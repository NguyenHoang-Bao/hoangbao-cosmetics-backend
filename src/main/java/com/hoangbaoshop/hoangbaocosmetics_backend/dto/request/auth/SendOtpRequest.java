package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Định dạng email không hợp lệ!")
    private String email;
}
