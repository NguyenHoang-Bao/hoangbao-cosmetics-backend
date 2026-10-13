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

    // tạo đơn hàng online (cho cả user đăng nhập hoặc khách vãng lai)
    OrderResponse createOrder(String username, CreateOrderRequest request);

    // lấy danh sách đơn hàng của tôi (user đăng nhập)
    PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable);

    // tra cứu đơn hàng theo mã đơn (kèm sđt bảo mật nếu là khách)
    OrderResponse getOrderByCode(String orderCode, String phoneNumber, String currentUsername);

    // lấy danh sách đơn hàng cho admin/staff kèm bộ lọc và phân trang
    PageResponse<OrderResponse> getAllOrdersAdmin(
            OrderStatus status,
            OrderType orderType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // lấy chi tiết đơn hàng by id cho admin/staff
    OrderResponse getOrderDetailAdmin(Integer orderId);

    // cập nhật trạng thái đơn hàng (duyệt, giao hàng, hoàn thành...)
    OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String staffUsername);

    // hủy đơn hàng từ phía admin/staff
    OrderResponse cancelOrderAdmin(Integer orderId, CancelOrderRequest request, String staffUsername);

    // tạo đơn hàng bán tại quầy (POS) cho nhân viên
    OrderResponse createPosOrder(CreatePosOrderRequest request, String staffUsername);

    // quét và tự động hủy các đơn chuyển khoản ngân hàng quá hạn thanh toán 30 phút, hoàn tồn kho
    int cancelExpiredBankTransferOrders();
}
