package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Danh mục không được để trống")
    private Integer idCategory;

    @NotNull(message = "Thương hiệu không được để trống")
    private Integer idBrand;

    private String description;
    private String benefits;
    private String keyIngredients;
    private String usageInstruction;
    private String thumbnail;

    @Valid
    private List<ProductVariantRequest> variants;
    private List<String> imageUrls;
}
