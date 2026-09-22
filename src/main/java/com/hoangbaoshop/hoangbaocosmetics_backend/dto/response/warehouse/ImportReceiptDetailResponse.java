package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptDetailResponse {

    private Integer idVariant;
    private String sku;
    private String capacity;
    private String productName;
    private Integer quantity;
    private Double importPrice;
    private Double subTotal;
}
