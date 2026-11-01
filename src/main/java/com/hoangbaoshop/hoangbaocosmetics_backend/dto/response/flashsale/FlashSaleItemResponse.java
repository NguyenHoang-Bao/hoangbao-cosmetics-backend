package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemResponse {

    private Integer idItem;
    private Integer idProduct;
    private String productName;
    private String thumbnail;
    private String categoryName;
    private String brandName;

    // Thông tin biến thể
    private Integer idVariant;
    private String sku;
    private String capacity;

    // Giá và chiết khấu
    private BigDecimal originalPrice;
    private BigDecimal regularPrice;
    private BigDecimal salePrice;
    private Integer discountPercent;

    // Tiến độ xả kho
    private Integer stockQuantity;
    private Integer soldQuantity;
    private Integer soldPercentage;
    private Boolean isSoldOut;
}
