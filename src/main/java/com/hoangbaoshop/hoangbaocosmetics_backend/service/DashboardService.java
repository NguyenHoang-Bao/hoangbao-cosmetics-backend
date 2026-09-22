package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardOverviewResponse getDashboardOverview();

    List<RevenueChartResponse> getMonthlyRevenue(int year);

    List<RevenueChartResponse> getDailyRevenue(LocalDate startDate, LocalDate endDate);

    List<OrderStatusDistributionResponse> getOrderStatusDistribution();

    List<TopSellingProductResponse> getTopSellingProducts(int limit);

    List<LowStockProductResponse> getLowStockProducts(int threshold);
}
