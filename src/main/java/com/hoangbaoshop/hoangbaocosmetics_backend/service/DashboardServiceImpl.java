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

import java.math.BigDecimal;
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

    // tổng quan dashboard: đếm tổng doanh thu, đơn hàng, khách hàng, hàng sắp hết
    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview() {
        BigDecimal totalRev = orderRepository.sumTotalRevenue();
        BigDecimal totalRevenue = totalRev != null ? totalRev : BigDecimal.ZERO;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal todayRev = orderRepository.sumRevenueBetween(startOfDay, endOfDay);
        BigDecimal todayRevenue = todayRev != null ? todayRev : BigDecimal.ZERO;

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

    // biểu đồ doanh thu theo 12 tháng (tự bù tháng doanh thu 0 để biểu đồ không bị gãy)
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
                    .revenue(BigDecimal.ZERO)
                    .orderCount(0L)
                    .build());
        }

        LocalDateTime startDate = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(targetYear + 1, 1, 1, 0, 0, 0);
        List<Object[]> rows = orderRepository.getMonthlyRevenueBetween(startDate, endDate);
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            BigDecimal rev = row[1] != null ? BigDecimal.valueOf(((Number) row[1]).doubleValue()) : BigDecimal.ZERO;
            long count = ((Number) row[2]).longValue();

            if (monthMap.containsKey(month)) {
                monthMap.get(month).setRevenue(rev);
                monthMap.get(month).setOrderCount(count);
            }
        }

        return new ArrayList<>(monthMap.values());
    }

    // biểu đồ doanh thu theo ngày (mặc định 30 ngày gần nhất)
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
                    .revenue(BigDecimal.ZERO)
                    .orderCount(0L)
                    .build());
            current = current.plusDays(1);
        }

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        List<Object[]> rows = orderRepository.getDailyRevenue(startDt, endDt);

        for (Object[] row : rows) {
            String dateKey = row[0].toString();
            BigDecimal rev = row[1] != null ? BigDecimal.valueOf(((Number) row[1]).doubleValue()) : BigDecimal.ZERO;
            long count = ((Number) row[2]).longValue();

            if (dateMap.containsKey(dateKey)) {
                dateMap.get(dateKey).setRevenue(rev);
                dateMap.get(dateKey).setOrderCount(count);
            }
        }

        return new ArrayList<>(dateMap.values());
    }

    // thống kê tỷ lệ phần trăm các trạng thái đơn hàng (vẽ biểu đồ tròn)
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusDistributionResponse> getOrderStatusDistribution() {
        List<Object[]> rows = orderRepository.countOrdersByStatusGrouped();
        Map<OrderStatus, Long> countMap = new EnumMap<>(OrderStatus.class);
        long totalOrders = 0;
        for (Object[] row : rows) {
            OrderStatus status = (OrderStatus) row[0];
            long count = ((Number) row[1]).longValue();
            countMap.put(status, count);
            totalOrders += count;
        }

        List<OrderStatusDistributionResponse> result = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = countMap.getOrDefault(status, 0L);
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

    // lấy top sản phẩm bán chạy nhất theo số lượng và doanh thu
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
            BigDecimal totalRev = row[5] != null ? BigDecimal.valueOf(((Number) row[5]).doubleValue()) : BigDecimal.ZERO;

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

    // cảnh báo các biến thể sản phẩm có số lượng tồn kho <= 10 (sắp hết hàng)
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
