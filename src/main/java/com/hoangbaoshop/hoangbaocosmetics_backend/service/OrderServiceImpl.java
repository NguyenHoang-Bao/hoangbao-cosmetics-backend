package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreateOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.OrderItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderDetailResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        String orderCode = generateOrderCode();

        User user = null;
        if (username != null && !username.isBlank()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        double totalProductPrice = 0.0;
        double shippingFee = 30000.0;

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .orderType(OrderType.ONLINE)
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .deliveryAddress(request.getDeliveryAddress())
                .shippingFee(shippingFee)
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .orderDetails(new ArrayList<>())
                .build();

        for (OrderItemRequest item : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getIdVariant())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getIdVariant()));

            if (variant.getStockQuantity() < item.getQuantity()) {
                String productName = variant.getProduct() != null ? variant.getProduct().getName() : "Sản phẩm";
                throw new BadRequestException("Sản phẩm '" + productName + " (" + variant.getCapacity() + ")' không đủ số lượng tồn kho (còn lại: " + variant.getStockQuantity() + ")");
            }

            // Trừ trực tiếp số lượng tồn kho
            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);

            double itemPrice = variant.getPrice();
            totalProductPrice += itemPrice * item.getQuantity();

            OrderDetail orderDetail = OrderDetail.builder()
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .price(itemPrice)
                    .isReviewed(false)
                    .build();

            order.addOrderDetail(orderDetail);
        }

        order.setTotalProductPrice(totalProductPrice);
        order.setTotalAmount(totalProductPrice + shippingFee);

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserUsername(username, pageable);
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNo(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));
        return mapToOrderResponse(order);
    }

    private String generateOrderCode() {
        String orderCode;
        do {
            orderCode = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (orderRepository.findByOrderCode(orderCode).isPresent());
        return orderCode;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderDetailResponse> items = order.getOrderDetails().stream()
                .map(detail -> {
                    ProductVariant variant = detail.getVariant();
                    String productName = (variant != null && variant.getProduct() != null)
                            ? variant.getProduct().getName()
                            : null;
                    String capacity = variant != null ? variant.getCapacity() : null;
                    String sku = variant != null ? variant.getSku() : null;
                    Double subTotal = (detail.getPrice() != null && detail.getQuantity() != null)
                            ? detail.getPrice() * detail.getQuantity()
                            : 0.0;

                    return OrderDetailResponse.builder()
                            .idOrderDetail(detail.getIdOrderDetail())
                            .idVariant(variant != null ? variant.getIdVariant() : null)
                            .productName(productName)
                            .capacity(capacity)
                            .sku(sku)
                            .quantity(detail.getQuantity())
                            .price(detail.getPrice())
                            .subTotal(subTotal)
                            .isReviewed(detail.getIsReviewed())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .idOrder(order.getIdOrder())
                .orderCode(order.getOrderCode())
                .idUser(order.getUser() != null ? order.getUser().getIdUser() : null)
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .orderType(order.getOrderType())
                .totalProductPrice(order.getTotalProductPrice())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .webhookStatus(order.getWebhookStatus())
                .status(order.getStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
