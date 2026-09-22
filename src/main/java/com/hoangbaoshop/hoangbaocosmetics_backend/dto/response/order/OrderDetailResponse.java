package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order;

import lombok.*;

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
    private Double price;
    private Boolean isReviewed;
}
