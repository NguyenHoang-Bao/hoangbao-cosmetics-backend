package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    private Integer idProduct;

    @NotNull(message = "ID chi tiết đơn hàng không được để trống")
    private Integer idOrderDetail;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @DecimalMin(value = "1.0", message = "Điểm đánh giá tối thiểu là 1.0")
    @DecimalMax(value = "5.0", message = "Điểm đánh giá tối đa là 5.0")
    private Float ratingPoint;

    private String content;
}
