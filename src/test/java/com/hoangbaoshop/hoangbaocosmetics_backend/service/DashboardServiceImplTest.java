package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.OrderStatusDistributionResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.dashboard.RevenueChartResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderDetailRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void testGetOrderStatusDistribution_UsesSingleGroupedQuery() {
        // Given
        List<Object[]> groupedResults = new ArrayList<>();
        groupedResults.add(new Object[]{OrderStatus.PENDING, 10L});
        groupedResults.add(new Object[]{OrderStatus.DELIVERED, 40L});

        when(orderRepository.countOrdersByStatusGrouped()).thenReturn(groupedResults);

        // When
        List<OrderStatusDistributionResponse> result = dashboardService.getOrderStatusDistribution();

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.values().length, result.size());

        OrderStatusDistributionResponse pending = result.stream()
                .filter(r -> r.getStatus() == OrderStatus.PENDING)
                .findFirst()
                .orElse(null);
        assertNotNull(pending);
        assertEquals(10L, pending.getOrderCount());
        assertEquals(20.0, pending.getPercentage()); // 10 / 50 * 100% = 20%

        OrderStatusDistributionResponse delivered = result.stream()
                .filter(r -> r.getStatus() == OrderStatus.DELIVERED)
                .findFirst()
                .orElse(null);
        assertNotNull(delivered);
        assertEquals(40L, delivered.getOrderCount());
        assertEquals(80.0, delivered.getPercentage()); // 40 / 50 * 100% = 80%

        // Verify only 1 query is executed
        verify(orderRepository, times(1)).countOrdersByStatusGrouped();
        verify(orderRepository, never()).count();
        verify(orderRepository, never()).countByStatus(any());
    }

    @Test
    void testGetMonthlyRevenue_CallsBetweenRangeQuery() {
        // Given
        int targetYear = 2026;
        LocalDateTime expectedStart = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(targetYear + 1, 1, 1, 0, 0, 0);

        List<Object[]> monthlyData = new ArrayList<>();
        monthlyData.add(new Object[]{10, 5000000.0, 15L});

        when(orderRepository.getMonthlyRevenueBetween(expectedStart, expectedEnd)).thenReturn(monthlyData);

        // When
        List<RevenueChartResponse> result = dashboardService.getMonthlyRevenue(targetYear);

        // Then
        assertNotNull(result);
        assertEquals(12, result.size());

        RevenueChartResponse oct = result.get(9); // month index 9 = Month 10
        assertEquals("Tháng 10/2026", oct.getPeriod());
        assertEquals(0, BigDecimal.valueOf(5000000).compareTo(oct.getRevenue()));
        assertEquals(15L, oct.getOrderCount());

        verify(orderRepository, times(1)).getMonthlyRevenueBetween(expectedStart, expectedEnd);
    }
}
