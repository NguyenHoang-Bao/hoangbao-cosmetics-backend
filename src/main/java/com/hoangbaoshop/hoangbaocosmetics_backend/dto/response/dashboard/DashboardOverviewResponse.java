package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewResponse {

    private Double totalRevenue;
    private Double todayRevenue;
    private Long totalOrders;
    private Long pendingOrders;
    private Long totalProducts;
    private Long totalCustomers;
    private Long lowStockVariants;
}
