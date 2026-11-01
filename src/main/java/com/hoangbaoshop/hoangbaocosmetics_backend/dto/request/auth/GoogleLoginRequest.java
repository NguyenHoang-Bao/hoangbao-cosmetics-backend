package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu xác thực đăng nhập bằng tài khoản Google")
public class GoogleLoginRequest {

    @Schema(description = "Google ID Token hoặc Google Access Token", example = "eyJhbGciOiJSUzI1NiIs...")
    @NotBlank(message = "Google token không được để trống")
    @JsonAlias({ "token", "accessToken" })
    private String idToken;
}
