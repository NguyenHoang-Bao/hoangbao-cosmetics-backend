package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Tìm kiếm và lọc sản phẩm có phân trang (Eager fetch Category & Brand chống N+1 query).
     */
    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.idCategory = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.idBrand = :brandId)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Integer categoryId,
                                 @Param("brandId") Integer brandId,
                                 Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"category", "brand"})
    Optional<Product> findById(Integer id);

    boolean existsByCategory_IdCategory(Integer idCategory);

    boolean existsByBrand_IdBrand(Integer idBrand);
}