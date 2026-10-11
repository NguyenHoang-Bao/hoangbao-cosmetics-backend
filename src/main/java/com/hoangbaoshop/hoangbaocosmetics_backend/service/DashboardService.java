package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    // tổng quan thống kê dashboard (tổng doanh thu, tổng đơn, tổng user, hàng sắp hết)
    DashboardOverviewResponse getDashboardOverview();

    // biểu đồ doanh thu theo 12 tháng trong năm
    List<RevenueChartResponse> getMonthlyRevenue(int year);

    // biểu đồ doanh thu theo từng ngày trong khoảng thời gian
    List<RevenueChartResponse> getDailyRevenue(LocalDate startDate, LocalDate endDate);

    // biểu đồ tròn tỷ lệ các trạng thái đơn hàng (chờ xử lý, đang giao, thành công...)
    List<OrderStatusDistributionResponse> getOrderStatusDistribution();

    // danh sách top sản phẩm bán chạy nhất
    List<TopSellingProductResponse> getTopSellingProducts(int limit);

    // danh sách sản phẩm tồn kho thấp (sắp hết hàng)
    List<LowStockProductResponse> getLowStockProducts(int threshold);
}
