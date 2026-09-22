package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview() {
        Double totalRev = orderRepository.sumTotalRevenue();
        double totalRevenue = totalRev != null ? totalRev : 0.0;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        Double todayRev = orderRepository.sumRevenueBetween(startOfDay, endOfDay);
        double todayRevenue = todayRev != null ? todayRev : 0.0;

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.countByRolesNameRole("CUSTOMER");
        long lowStockVariants = productVariantRepository.countByStockQuantityLessThanEqual(10);

        return DashboardOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .totalProducts(totalProducts)
                .totalCustomers(totalCustomers)
                .lowStockVariants(lowStockVariants)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueChartResponse> getMonthlyRevenue(int year) {
        int targetYear = year > 0 ? year : LocalDate.now().getYear();

        // Khởi tạo trước 12 tháng với doanh thu = 0 để biểu đồ hiển thị liên tục
        Map<Integer, RevenueChartResponse> monthMap = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String label = String.format("Tháng %02d/%d", m, targetYear);
            monthMap.put(m, RevenueChartResponse.builder()
                    .period(label)
                    .revenue(0.0)
                    .orderCount(0L)
                    .build());
        }

        List<Object[]> rows = orderRepository.getMonthlyRevenue(targetYear);
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            double rev = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();

            if (monthMap.containsKey(month)) {
                monthMap.get(month).setRevenue(rev);
                monthMap.get(month).setOrderCount(count);
            }
        }

        return new ArrayList<>(monthMap.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueChartResponse> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(29); // Mặc định 30 ngày gần nhất

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        // Khởi tạo các ngày liên tục với doanh thu 0
        Map<String, RevenueChartResponse> dateMap = new LinkedHashMap<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            String key = current.toString();
            dateMap.put(key, RevenueChartResponse.builder()
                    .period(key)
                    .revenue(0.0)
                    .orderCount(0L)
                    .build());
            current = current.plusDays(1);
        }

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        List<Object[]> rows = orderRepository.getDailyRevenue(startDt, endDt);

        for (Object[] row : rows) {
            String dateKey = row[0].toString();
            double rev = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();

            if (dateMap.containsKey(dateKey)) {
                dateMap.get(dateKey).setRevenue(rev);
                dateMap.get(dateKey).setOrderCount(count);
            }
        }

        return new ArrayList<>(dateMap.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusDistributionResponse> getOrderStatusDistribution() {
        long totalOrders = orderRepository.count();
        List<OrderStatusDistributionResponse> result = new ArrayList<>();

        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            double percentage = totalOrders > 0
                    ? Math.round(((double) count / totalOrders * 100.0) * 10.0) / 10.0
                    : 0.0;

            result.add(OrderStatusDistributionResponse.builder()
                    .status(status)
                    .orderCount(count)
                    .percentage(percentage)
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        int maxLimit = limit > 0 ? limit : 5;
        Pageable pageable = PageRequest.of(0, maxLimit);
        List<Object[]> rows = orderDetailRepository.findTopSellingProducts(pageable);

        return rows.stream().map(row -> {
            Integer idProduct = (Integer) row[0];
            String productName = (String) row[1];
            String thumbnail = (String) row[2];
            String categoryName = (String) row[3];
            Long totalSold = ((Number) row[4]).longValue();
            Double totalRev = ((Number) row[5]).doubleValue();

            return TopSellingProductResponse.builder()
                    .idProduct(idProduct)
                    .productName(productName)
                    .thumbnail(thumbnail)
                    .categoryName(categoryName)
                    .totalQuantitySold(totalSold)
                    .totalRevenue(totalRev)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockProductResponse> getLowStockProducts(int threshold) {
        int targetThreshold = threshold > 0 ? threshold : 10;
        List<ProductVariant> variants = productVariantRepository.findLowStockVariants(targetThreshold);

        return variants.stream().map(v -> {
            Product p = v.getProduct();
            return LowStockProductResponse.builder()
                    .idVariant(v.getIdVariant())
                    .idProduct(p != null ? p.getIdProduct() : null)
                    .sku(v.getSku())
                    .capacity(v.getCapacity())
                    .productName(p != null ? p.getName() : null)
                    .thumbnail(p != null ? p.getThumbnail() : null)
                    .stockQuantity(v.getStockQuantity())
                    .price(v.getPrice())
                    .build();
        }).collect(Collectors.toList());
    }
}
