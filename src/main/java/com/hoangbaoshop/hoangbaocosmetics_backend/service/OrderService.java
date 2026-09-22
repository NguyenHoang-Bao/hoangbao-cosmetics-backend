package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CancelOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreateOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreatePosOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.UpdateOrderStatusRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {

    // Khách hàng / Public
    OrderResponse createOrder(String username, CreateOrderRequest request);

    PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable);

    OrderResponse getOrderByCode(String orderCode);

    // Admin / Staff
    PageResponse<OrderResponse> getAllOrdersAdmin(
            OrderStatus status,
            OrderType orderType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    OrderResponse getOrderDetailAdmin(Integer orderId);

    OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String staffUsername);

    OrderResponse cancelOrderAdmin(Integer orderId, CancelOrderRequest request, String staffUsername);

    OrderResponse createPosOrder(CreatePosOrderRequest request, String staffUsername);
}
