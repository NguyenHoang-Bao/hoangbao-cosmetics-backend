package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreateOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(String username, CreateOrderRequest request);

    PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable);

    OrderResponse getOrderByCode(String orderCode);
}
