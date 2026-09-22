package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    Optional<ProductVariant> findBySku(String sku);

    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= :threshold ORDER BY v.stockQuantity ASC")
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);

    long countByStockQuantityLessThanEqual(int threshold);
}
