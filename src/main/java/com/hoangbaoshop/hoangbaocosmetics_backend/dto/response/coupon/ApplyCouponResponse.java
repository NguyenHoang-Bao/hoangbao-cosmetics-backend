package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.DiscountType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponResponse {

    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private String message;
}
