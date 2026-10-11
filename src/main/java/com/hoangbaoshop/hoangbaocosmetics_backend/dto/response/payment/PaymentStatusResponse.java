package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {

    private String orderCode;
    private boolean isPaid;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String transactionId;
    private PaymentMethod paymentMethod;
}
