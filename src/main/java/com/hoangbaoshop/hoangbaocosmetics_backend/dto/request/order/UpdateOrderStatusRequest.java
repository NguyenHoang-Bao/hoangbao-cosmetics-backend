package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;

    private String note;
}
