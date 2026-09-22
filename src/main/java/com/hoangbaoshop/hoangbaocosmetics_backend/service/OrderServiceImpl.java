package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.*;
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

import java.time.LocalDateTime;
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

    // ==================== ADMIN & STAFF METHODS ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrdersAdmin(
            OrderStatus status,
            OrderType orderType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Order> page = orderRepository.searchOrdersAdmin(status, orderType, trimmedKeyword, startDate, endDate, pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailAdmin(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String staffUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new BadRequestException("Đơn hàng đã giao thành công, không thể thay đổi trạng thái!");
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy, không thể thay đổi trạng thái!");
        }
        if (currentStatus == newStatus) {
            return mapToOrderResponse(order);
        }

        // Kiểm tra hợp lệ của luồng chuyển đổi trạng thái
        validateStatusTransition(currentStatus, newStatus);

        // Nếu chuyển sang trạng thái CANCELLED -> Hoàn lại tồn kho
        if (newStatus == OrderStatus.CANCELLED) {
            rollbackStockForOrder(order);
        }

        order.setStatus(newStatus);
        if (request.getNote() != null && !request.getNote().isBlank()) {
            String existingNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(existingNote + request.getNote().trim());
        }

        if (staffUsername != null && !staffUsername.isBlank()) {
            User staff = userRepository.findByUsername(staffUsername).orElse(null);
            order.setStaff(staff);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderAdmin(Integer orderId, CancelOrderRequest request, String staffUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Đơn hàng đã giao thành công, không thể hủy!");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng này đã bị hủy trước đó!");
        }

        // Hoàn lại tồn kho cho tất cả các sản phẩm trong đơn
        rollbackStockForOrder(order);

        order.setStatus(OrderStatus.CANCELLED);
        String cancelNote = "[HỦY ĐƠN]: " + request.getReason().trim();
        String existingNote = order.getNote() != null ? order.getNote() + " | " : "";
        order.setNote(existingNote + cancelNote);

        if (staffUsername != null && !staffUsername.isBlank()) {
            User staff = userRepository.findByUsername(staffUsername).orElse(null);
            order.setStaff(staff);
        }

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse createPosOrder(CreatePosOrderRequest request, String staffUsername) {
        String orderCode = generateOrderCode();

        User staff = null;
        if (staffUsername != null && !staffUsername.isBlank()) {
            staff = userRepository.findByUsername(staffUsername).orElse(null);
        }

        double totalProductPrice = 0.0;
        double shippingFee = 0.0; // Bán tại quầy không tính phí vận chuyển

        String customerName = (request.getCustomerName() != null && !request.getCustomerName().isBlank())
                ? request.getCustomerName().trim()
                : "Khách mua tại quầy";

        Order order = Order.builder()
                .orderCode(orderCode)
                .staff(staff)
                .orderType(OrderType.OFFLINE)
                .customerName(customerName)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : "TẠI QUẦY")
                .deliveryAddress("Mua tại cửa hàng")
                .shippingFee(shippingFee)
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.DELIVERED) // Khách mua tại quầy nhận hàng ngay
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

    // ==================== HELPER METHODS ====================

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case PENDING:
                if (next != OrderStatus.PROCESSING && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'PENDING', chỉ có thể chuyển sang 'PROCESSING' hoặc 'CANCELLED'!");
                }
                break;
            case PROCESSING:
                if (next != OrderStatus.SHIPPING && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'PROCESSING', chỉ có thể chuyển sang 'SHIPPING' hoặc 'CANCELLED'!");
                }
                break;
            case SHIPPING:
                if (next != OrderStatus.DELIVERED && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'SHIPPING', chỉ có thể chuyển sang 'DELIVERED' hoặc 'CANCELLED' (hàng trả lại/giao thất bại)!");
                }
                break;
            default:
                throw new BadRequestException("Không thể chuyển đổi từ trạng thái " + current + " sang " + next);
        }
    }

    private void rollbackStockForOrder(Order order) {
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = detail.getVariant();
                if (variant != null) {
                    variant.setStockQuantity(variant.getStockQuantity() + detail.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        }
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
