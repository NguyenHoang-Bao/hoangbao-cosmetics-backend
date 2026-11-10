package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.ApplyCouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.CouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.ApplyCouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.CouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Coupon;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.DiscountType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponServiceImpl couponService;

    @Test
    @DisplayName("Áp dụng mã giảm giá phần trăm thành công và bị chặn bởi trần max discount")
    void applyCoupon_Percentage_SuccessWithCap() {
        Coupon coupon = Coupon.builder()
                .idCoupon(1)
                .code("VIP20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .minOrderAmount(BigDecimal.valueOf(500000))
                .maxDiscountAmount(BigDecimal.valueOf(100000))
                .isActive(true)
                .usedCount(5)
                .usageLimit(100)
                .build();

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("VIP20")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .code("VIP20")
                .orderAmount(BigDecimal.valueOf(1000000)) // 20% của 1.000.000 = 200.000 -> trần 100.000
                .build();

        ApplyCouponResponse response = couponService.applyCoupon(request);

        assertNotNull(response);
        assertEquals("VIP20", response.getCode());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(900000).compareTo(response.getFinalAmount()));
    }

    @Test
    @DisplayName("Áp dụng mã giảm giá tiền mặt cố định thành công")
    void applyCoupon_FixedAmount_Success() {
        Coupon coupon = Coupon.builder()
                .idCoupon(2)
                .code("FREESHIP")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(30000))
                .minOrderAmount(BigDecimal.valueOf(200000))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("FREESHIP")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .code("FREESHIP")
                .orderAmount(BigDecimal.valueOf(350000))
                .build();

        ApplyCouponResponse response = couponService.applyCoupon(request);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(response.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(320000).compareTo(response.getFinalAmount()));
    }

    @Test
    @DisplayName("Báo lỗi khi đơn hàng chưa đạt giá trị tối thiểu của mã")
    void applyCoupon_BelowMinOrderAmount_ThrowsException() {
        Coupon coupon = Coupon.builder()
                .idCoupon(3)
                .code("HB50K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(50000))
                .minOrderAmount(BigDecimal.valueOf(500000))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("HB50K")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .code("HB50K")
                .orderAmount(BigDecimal.valueOf(300000)) // 300K < 500K min
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> couponService.applyCoupon(request));
        assertTrue(ex.getMessage().contains("Đơn hàng phải từ"));
    }

    @Test
    @DisplayName("Báo lỗi khi mã giảm giá đã hết hạn sử dụng")
    void applyCoupon_Expired_ThrowsException() {
        Coupon coupon = Coupon.builder()
                .idCoupon(4)
                .code("EXPIRED")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(20000))
                .endDate(LocalDateTime.now().minusDays(1))
                .isActive(true)
                .build();

        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("EXPIRED")).thenReturn(Optional.of(coupon));

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .code("EXPIRED")
                .orderAmount(BigDecimal.valueOf(500000))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> couponService.applyCoupon(request));
        assertTrue(ex.getMessage().contains("đã hết hạn"));
    }

    @Test
    @DisplayName("Báo lỗi khi mã không tồn tại")
    void applyCoupon_NotFound_ThrowsException() {
        when(couponRepository.findByCodeIgnoreCaseAndIsActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .code("UNKNOWN")
                .orderAmount(BigDecimal.valueOf(500000))
                .build();

        assertThrows(ResourceNotFoundException.class, () -> couponService.applyCoupon(request));
    }

    @Test
    @DisplayName("Admin tạo mới voucher thành công")
    void createCoupon_Success() {
        when(couponRepository.existsByCodeIgnoreCase("NEWYEAR")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
            Coupon c = invocation.getArgument(0);
            c.setIdCoupon(10);
            return c;
        });

        CouponRequest request = CouponRequest.builder()
                .code("NEWYEAR")
                .description("Mừng năm mới")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15))
                .minOrderAmount(BigDecimal.valueOf(300000))
                .build();

        CouponResponse response = couponService.createCoupon(request);

        assertNotNull(response);
        assertEquals("NEWYEAR", response.getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }
}
