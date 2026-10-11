package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewResponse {

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long totalProducts;
    private Long totalCustomers;
    private Long lowStockVariants;
}
