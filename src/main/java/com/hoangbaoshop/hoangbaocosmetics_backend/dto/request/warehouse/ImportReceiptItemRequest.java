package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptItemRequest {

    @NotNull(message = "ID biến thể không được để trống")
    private Integer idVariant;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập phải lớn hơn hoặc bằng 1")
    private Integer quantity;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "0.0", message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private Double importPrice;
}
