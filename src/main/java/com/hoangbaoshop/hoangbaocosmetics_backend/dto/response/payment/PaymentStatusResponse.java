package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {

    private String orderCode;
    private boolean isPaid;
    private OrderStatus status;
    private Double totalAmount;
    private String transactionId;
    private PaymentMethod paymentMethod;
}
