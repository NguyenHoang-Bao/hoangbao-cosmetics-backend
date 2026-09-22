package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {

    @NotBlank(message = "Vui lòng nhập lý do hủy đơn hàng")
    private String reason;
}
