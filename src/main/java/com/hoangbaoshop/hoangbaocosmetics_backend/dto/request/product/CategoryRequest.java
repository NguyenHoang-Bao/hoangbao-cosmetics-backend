package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String slug;
}
