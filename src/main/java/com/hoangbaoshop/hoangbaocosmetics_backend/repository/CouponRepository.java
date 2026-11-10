package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    // Tìm kiếm mã giảm giá theo code và trạng thái kích hoạt
    Optional<Coupon> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    // Tìm kiếm mã giảm giá theo code bất kể trạng thái
    Optional<Coupon> findByCodeIgnoreCase(String code);

    // Lấy toàn bộ danh sách mã giảm giá đang kích hoạt
    List<Coupon> findByIsActiveTrueOrderByCreatedAtDesc();

    // Kiểm tra trùng lặp code khi tạo mới
    boolean existsByCodeIgnoreCase(String code);
}
