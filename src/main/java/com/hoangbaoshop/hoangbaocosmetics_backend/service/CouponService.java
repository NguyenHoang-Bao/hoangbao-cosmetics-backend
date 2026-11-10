package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.ApplyCouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.CouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.ApplyCouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.CouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Coupon;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    // Lấy danh sách voucher đang kích hoạt cho khách hàng chọn
    List<CouponResponse> getActiveCoupons();

    // Kiểm tra tính hợp lệ và tính toán số tiền giảm giá khi khách áp mã tại Checkout
    ApplyCouponResponse applyCoupon(ApplyCouponRequest request);

    // Tính toán số tiền giảm giá từ đối tượng Coupon (dùng trong OrderService)
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount);

    // Kiểm tra và lấy entity Coupon hợp lệ để áp dụng vào đơn hàng (dùng trong OrderService)
    Coupon validateAndGetCoupon(String code, BigDecimal orderAmount);

    // Tăng số lượt đã sử dụng của voucher
    void incrementUsage(Coupon coupon);

    // ================= ADMIN APIS =================
    PageResponse<CouponResponse> getAllCouponsAdmin(Pageable pageable);

    CouponResponse getCouponById(Integer id);

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Integer id, CouponRequest request);

    void deleteCoupon(Integer id);
}
