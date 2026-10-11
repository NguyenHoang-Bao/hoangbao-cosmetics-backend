package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CancelOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreatePosOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.UpdateOrderStatusRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'ONLINE_STAFF', 'WAREHOUSE_MANAGER')")
public class AdminOrderController {

    private final OrderService orderService;

    // [GET] lấy danh sách tất cả đơn hàng cho admin/staff kèm bộ lọc và phân trang
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<OrderResponse> response = orderService.getAllOrdersAdmin(status, orderType, keyword, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [GET] xem chi tiết đơn hàng by id cho admin/staff
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable Integer id) {
        OrderResponse response = orderService.getOrderDetailAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [PATCH] cập nhật trạng thái đơn hàng (duyệt, giao hàng, hoàn thành...)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String staffUsername = userDetails != null ? userDetails.getUsername() : null;
        OrderResponse response = orderService.updateOrderStatus(id, request, staffUsername);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái đơn hàng thành công!"));
    }

    // [POST] hủy đơn hàng và hoàn lại tồn kho (Admin / Staff)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Integer id,
            @Valid @RequestBody CancelOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String staffUsername = userDetails != null ? userDetails.getUsername() : null;
        OrderResponse response = orderService.cancelOrderAdmin(id, request, staffUsername);
        return ResponseEntity.ok(ApiResponse.success(response, "Hủy đơn hàng và hoàn tồn kho thành công!"));
    }

    // [POST] tạo đơn hàng bán tại quầy POS (trừ kho và hoàn thành ngay)
    @PostMapping("/pos")
    public ResponseEntity<ApiResponse<OrderResponse>> createPosOrder(
            @Valid @RequestBody CreatePosOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String staffUsername = userDetails != null ? userDetails.getUsername() : null;
        OrderResponse response = orderService.createPosOrder(request, staffUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo đơn hàng tại quầy thành công!"));
    }
}
