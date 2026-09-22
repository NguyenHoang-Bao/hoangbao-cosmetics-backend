package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePosOrderRequest {

    @Builder.Default
    private String customerName = "Khách mua tại quầy";

    private String phoneNumber;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> items;
}
