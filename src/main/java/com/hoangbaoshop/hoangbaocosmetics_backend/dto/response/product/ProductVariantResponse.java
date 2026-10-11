package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {

    private Integer idVariant;
    private String sku;
    private String barcode;
    private String capacity;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer shelfStock;
    private Integer centralStock;
    private Integer shelfMinAlert;
    private Integer shelfCapacity;
}
