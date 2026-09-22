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

    Page<Review> findByProductIdProduct(Integer productId, Pageable pageable);

    Page<Review> findByUserUsername(String username, Pageable pageable);

    boolean existsByOrderDetailIdOrderDetail(Integer idOrderDetail);

    long countByProductIdProduct(Integer productId);

    @Query("SELECT COALESCE(AVG(r.ratingPoint), 0.0) FROM Review r WHERE r.product.idProduct = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Integer productId);
}
