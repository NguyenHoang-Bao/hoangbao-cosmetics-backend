package com.hoangbaoshop.hoangbaocosmetics_backend.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEmailDto {

    private String orderCode;
    private String customerName;
    private String phoneNumber;
    private String deliveryAddress;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String productName;
        private String capacity;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }
}
