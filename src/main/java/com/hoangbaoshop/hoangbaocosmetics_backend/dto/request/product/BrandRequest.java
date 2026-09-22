package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    private String logoUrl;
}
