package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacebookLoginRequest {
    @NotBlank(message = "Facebook Access Token không được để trống")
    private String accessToken;
}
