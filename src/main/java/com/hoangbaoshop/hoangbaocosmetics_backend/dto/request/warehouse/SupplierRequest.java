package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    private String phone;

    private String address;
}
