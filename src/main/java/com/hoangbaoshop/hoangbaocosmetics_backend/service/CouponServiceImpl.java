package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.ApplyCouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.coupon.CouponRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.ApplyCouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.coupon.CouponResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Coupon;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.DiscountType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .filter(c -> (c.getStartDate() == null || !now.isBefore(c.getStartDate())) &&
                             (c.getEndDate() == null || !now.isAfter(c.getEndDate())) &&
                             (c.getUsageLimit() == null || c.getUsedCount() < c.getUsageLimit()))
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Coupon coupon = validateAndGetCoupon(request.getCode(), request.getOrderAmount());
        BigDecimal discount = calculateDiscount(coupon, request.getOrderAmount());
        BigDecimal finalAmount = request.getOrderAmount().subtract(discount).max(BigDecimal.ZERO);

        return ApplyCouponResponse.builder()
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .discountAmount(discount)
                .originalAmount(request.getOrderAmount())
                .finalAmount(finalAmount)
                .message("Áp dụng mã giảm giá thành công!")
                .build();
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            // Giá trị giảm = (orderAmount * discountValue) / 100
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(coupon.getMaxDiscountAmount());
            }
        } else {
            // Giảm tiền mặt cố định
            discount = coupon.getDiscountValue();
        }

        // Không được giảm vượt quá tổng giá trị đơn hàng
        return discount.min(orderAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon validateAndGetCoupon(String code, BigDecimal orderAmount) {
        if (code == null || code.trim().isBlank()) {
            throw new BadRequestException("Mã giảm giá không hợp lệ");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá '" + code.trim() + "' không tồn tại hoặc đã ngừng áp dụng"));

        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra ngày bắt đầu
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new BadRequestException("Mã giảm giá '" + coupon.getCode() + "' chưa đến thời gian áp dụng");
        }

        // 2. Kiểm tra ngày kết thúc
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new BadRequestException("Mã giảm giá '" + coupon.getCode() + "' đã hết hạn sử dụng");
        }

        // 3. Kiểm tra số lượt dùng
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá '" + coupon.getCode() + "' đã hết số lượt sử dụng");
        }

        // 4. Kiểm tra đơn hàng tối thiểu
        if (coupon.getMinOrderAmount() != null && coupon.getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (orderAmount == null || orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new BadRequestException(String.format("Đơn hàng phải từ %,.0f₫ để áp dụng mã giảm giá này",
                        coupon.getMinOrderAmount().doubleValue()));
            }
        }

        return coupon;
    }

    @Override
    @Transactional
    public void incrementUsage(Coupon coupon) {
        coupon.setUsedCount((coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) + 1);
        couponRepository.save(coupon);
    }

    // ================= ADMIN APIS =================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> getAllCouponsAdmin(Pageable pageable) {
        Page<Coupon> page = couponRepository.findAll(pageable);
        List<CouponResponse> content = page.getContent().stream()
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());

        return PageResponse.<CouponResponse>builder()
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
    public CouponResponse getCouponById(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
        return mapToCouponResponse(coupon);
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new BadRequestException("Mã giảm giá '" + request.getCode().trim() + "' đã tồn tại trong hệ thống");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().trim().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("Admin đã tạo mã voucher mới: {}", saved.getCode());
        return mapToCouponResponse(saved);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Integer id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));

        // Kiểm tra nếu đổi code sang mã khác đã tồn tại
        if (!coupon.getCode().equalsIgnoreCase(request.getCode().trim()) &&
                couponRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new BadRequestException("Mã giảm giá '" + request.getCode().trim() + "' đã tồn tại");
        }

        coupon.setCode(request.getCode().trim().toUpperCase());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }

        Coupon updated = couponRepository.save(coupon);
        log.info("Admin đã cập nhật mã voucher ID: {}", id);
        return mapToCouponResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCoupon(Integer id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với ID: " + id));
        couponRepository.delete(coupon);
        log.info("Admin đã xóa mã voucher ID: {}", id);
    }

    private CouponResponse mapToCouponResponse(Coupon c) {
        return CouponResponse.builder()
                .idCoupon(c.getIdCoupon())
                .code(c.getCode())
                .description(c.getDescription())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .usageLimit(c.getUsageLimit())
                .usedCount(c.getUsedCount())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
