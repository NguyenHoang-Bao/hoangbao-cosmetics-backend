package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse;

import lombok.*;

import java.math.BigDecimal;

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
    private BigDecimal importPrice;
    private BigDecimal subTotal;
}
