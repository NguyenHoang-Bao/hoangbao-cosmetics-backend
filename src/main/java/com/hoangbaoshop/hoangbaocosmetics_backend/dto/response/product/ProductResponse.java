package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Integer idProduct;
    private String name;
    private CategoryResponse category;
    private BrandResponse brand;
    private String description;
    private String benefits;
    private String keyIngredients;
    private String usageInstruction;
    private String thumbnail;
    private List<ProductVariantResponse> variants;
    private List<String> images;
}
