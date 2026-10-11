package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Integer idOrder;
    private String orderCode;
    private Integer idUser;
    private String customerName;
    private String phoneNumber;
    private String deliveryAddress;
    private OrderType orderType;
    private BigDecimal totalProductPrice;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private String webhookStatus;
    private OrderStatus status;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> items;
}
