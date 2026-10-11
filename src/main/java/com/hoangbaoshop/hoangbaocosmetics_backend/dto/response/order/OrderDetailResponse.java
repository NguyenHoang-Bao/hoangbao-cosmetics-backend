package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {

    private Integer idOrderDetail;
    private Integer idVariant;
    private String productName;
    private String sku;
    private String capacity;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
    private Boolean isReviewed;
}
