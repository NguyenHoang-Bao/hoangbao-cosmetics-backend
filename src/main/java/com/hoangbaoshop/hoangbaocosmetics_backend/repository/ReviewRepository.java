package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // lấy danh sách đánh giá của sản phẩm có phân trang
    Page<Review> findByProductIdProduct(Integer productId, Pageable pageable);

    // lấy danh sách đánh giá của user có phân trang
    Page<Review> findByUserUsername(String username, Pageable pageable);

    // kiểm tra đơn hàng chi tiết đã được đánh giá chưa
    boolean existsByOrderDetailIdOrderDetail(Integer idOrderDetail);

    // đếm tổng số lượng đánh giá của sản phẩm
    long countByProductIdProduct(Integer productId);

    // tính điểm đánh giá trung bình của sản phẩm
    @Query("SELECT COALESCE(AVG(r.ratingPoint), 0.0) FROM Review r WHERE r.product.idProduct = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Integer productId);
}
