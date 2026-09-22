package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockProductResponse {

    private Integer idVariant;
    private Integer idProduct;
    private String sku;
    private String capacity;
    private String productName;
    private String thumbnail;
    private Integer stockQuantity;
    private Double price;
}
