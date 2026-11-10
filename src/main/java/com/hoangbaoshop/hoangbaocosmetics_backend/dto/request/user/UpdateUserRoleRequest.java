package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRoleRequest {

    @NotBlank(message = "Tên vai trò không được để trống")
    private String roleName;
}
