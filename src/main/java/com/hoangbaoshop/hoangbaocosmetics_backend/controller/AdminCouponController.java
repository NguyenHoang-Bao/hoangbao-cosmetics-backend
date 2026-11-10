package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.CouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.CouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Coupon Management", description = "Các API tạo, sửa, xóa mã giảm giá dành cho Quản trị viên")
public class AdminCouponController {

    private final CouponService couponService;

    // [GET] /api/admin/coupons - Danh sách tất cả mã giảm giá (phân trang)
    @GetMapping
    @Operation(summary = "Lấy danh sách mã giảm giá phân trang (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<CouponResponse>>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(couponService.getAllCouponsAdmin(pageable)));
    }

    // [GET] /api/admin/coupons/{id} - Xem chi tiết mã giảm giá
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết mã giảm giá theo ID (Admin)")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCouponById(id)));
    }

    // [POST] /api/admin/coupons - Thêm mới mã giảm giá
    @PostMapping
    @Operation(summary = "Tạo mã giảm giá mới (Admin)")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo mã giảm giá thành công!"));
    }

    // [PUT] /api/admin/coupons/{id} - Cập nhật thông tin mã giảm giá
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật mã giảm giá (Admin)")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Integer id,
            @Valid @RequestBody CouponRequest request
    ) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật mã giảm giá thành công!"));
    }

    
    // [PATCH] /api/admin/coupons/{id}/status - Bật / tắt trạng thái kích hoạt mã giảm giá
    @PatchMapping("/{id}/status")
    @Operation(summary = "Kích hoạt / Vô hiệu hóa mã giảm giá (Admin)")
    public ResponseEntity<ApiResponse<CouponResponse>> toggleCouponStatus(@PathVariable Integer id) {
        CouponResponse response = couponService.toggleCouponStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái mã giảm giá thành công!"));
    }

    // [DELETE] /api/admin/coupons/{id} - Xóa mã giảm giá
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mã giảm giá (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Integer id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa mã giảm giá thành công!"));
    }
}
