package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isPaid")
    private boolean isPaid;

    private OrderStatus status;
    private BigDecimal totalAmount;
    private String transactionId;
    private PaymentMethod paymentMethod;

    @JsonProperty("isPaid")
    public boolean isPaid() {
        return isPaid;
    }
}
