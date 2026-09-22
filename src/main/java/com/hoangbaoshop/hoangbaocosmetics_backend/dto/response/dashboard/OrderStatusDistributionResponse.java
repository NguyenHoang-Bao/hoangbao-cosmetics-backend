package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusDistributionResponse {

    private OrderStatus status;
    private Long orderCount;
    private Double percentage;
}
