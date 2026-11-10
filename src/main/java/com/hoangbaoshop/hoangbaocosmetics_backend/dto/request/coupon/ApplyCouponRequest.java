package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @PositiveOrZero(message = "Giá trị đơn hàng phải lớn hơn hoặc bằng 0")
    private BigDecimal orderAmount;
}
