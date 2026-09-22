package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'WAREHOUSE_MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    // 1. Tổng quan các chỉ số KPI nhanh
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview() {
        DashboardOverviewResponse response = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2. Biểu đồ doanh thu theo 12 tháng trong năm
    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<List<RevenueChartResponse>>> getMonthlyRevenue(
            @RequestParam(defaultValue = "0") int year
    ) {
        List<RevenueChartResponse> response = dashboardService.getMonthlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Biểu đồ doanh thu theo ngày trong khoảng thời gian (mặc định 30 ngày gần nhất)
    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<List<RevenueChartResponse>>> getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<RevenueChartResponse> response = dashboardService.getDailyRevenue(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. Phân bổ số lượng đơn hàng theo trạng thái
    @GetMapping("/order-status")
    public ResponseEntity<ApiResponse<List<OrderStatusDistributionResponse>>> getOrderStatusDistribution() {
        List<OrderStatusDistributionResponse> response = dashboardService.getOrderStatusDistribution();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 5. Top sản phẩm bán chạy nhất
    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<List<TopSellingProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<TopSellingProductResponse> response = dashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 6. Danh sách cảnh báo biến thể sản phẩm sắp hết hàng
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold
    ) {
        List<LowStockProductResponse> response = dashboardService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
