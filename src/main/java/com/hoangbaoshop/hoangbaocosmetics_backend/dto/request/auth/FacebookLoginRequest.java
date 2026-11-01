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
@Schema(description = "Yêu cầu xác thực đăng nhập bằng tài khoản Facebook")
public class FacebookLoginRequest {

    @Schema(description = "Facebook Access Token từ Facebook JavaScript SDK", example = "EAAB...")
    @NotBlank(message = "Facebook Access Token không được để trống")
    @JsonAlias({ "token" })
    private String accessToken;
}
