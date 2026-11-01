package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemRequest {

    @NotNull(message = "ID biến thể không được để trống")
    private Integer idVariant;

    @NotNull(message = "Giá Flash Sale không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá Flash Sale phải lớn hơn 0")
    private BigDecimal salePrice;

    @NotNull(message = "Số lượng giới hạn không được để trống")
    @Min(value = 1, message = "Số lượng giới hạn phải lớn hơn hoặc bằng 1")
    private Integer stockQuantity;
}
