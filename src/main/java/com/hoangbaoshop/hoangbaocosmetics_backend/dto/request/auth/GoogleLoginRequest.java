package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    @NotBlank(message = "Google ID token không được để trống")
    private String idToken;
}
