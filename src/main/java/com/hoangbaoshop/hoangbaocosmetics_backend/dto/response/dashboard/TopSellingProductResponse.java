package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductResponse {

    private Integer idProduct;
    private String productName;
    private String thumbnail;
    private String categoryName;
    private Long totalQuantitySold;
    private Double totalRevenue;
}
