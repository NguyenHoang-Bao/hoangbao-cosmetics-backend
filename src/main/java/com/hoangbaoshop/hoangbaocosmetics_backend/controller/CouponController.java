package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.ApplyCouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.ApplyCouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.CouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon & Voucher Management", description = "Các API truy vấn và áp dụng mã giảm giá cho khách hàng")
public class CouponController {

    private final CouponService couponService;

    // [GET] /api/coupons - Lấy danh sách các voucher đang kích hoạt và khả dụng
    @GetMapping
    @Operation(summary = "Lấy danh sách mã giảm giá khả dụng (Public)", description = "Dành cho khách xem mã giảm giá tại trang Checkout hoặc Flash Sale")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getActiveCoupons() {
        List<CouponResponse> response = couponService.getActiveCoupons();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [POST] /api/coupons/apply - Kiểm tra và tính toán giảm giá khi khách nhập mã tại trang Checkout
    @PostMapping("/apply")
    @Operation(summary = "Kiểm tra và áp dụng mã giảm giá (Public)", description = "Tính toán số tiền giảm giá và số tiền thanh toán cuối cùng trước khi đặt hàng")
    public ResponseEntity<ApiResponse<ApplyCouponResponse>> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        ApplyCouponResponse response = couponService.applyCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }
}
