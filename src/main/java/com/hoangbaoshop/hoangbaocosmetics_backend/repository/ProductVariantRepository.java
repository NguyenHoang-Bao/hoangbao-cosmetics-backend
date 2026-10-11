package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // tìm biến thể sản phẩm theo SKU
    Optional<ProductVariant> findBySku(String sku);

    // tìm các biến thể sắp hết hàng theo ngưỡng tồn kho (phục vụ cảnh báo)
    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= :threshold ORDER BY v.stockQuantity ASC")
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);

    // đếm số lượng biến thể có tồn kho nhỏ hơn hoặc bằng ngưỡng
    long countByStockQuantityLessThanEqual(int threshold);

    // tìm biến thể và join fetch sản phẩm theo idVariant
    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.idVariant = :idVariant")
    Optional<ProductVariant> findByIdWithProduct(@Param("idVariant") Integer idVariant);

    // tìm danh sách biến thể và join fetch sản phẩm theo danh sách idVariant
    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.idVariant IN :ids")
    List<ProductVariant> findAllWithProductByIdIn(@Param("ids") Collection<Integer> ids);

    // trừ tồn kho nguyên tử ở database chống âm kho (Atomic Update)
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity - :quantity " +
           "WHERE v.idVariant = :idVariant AND v.stockQuantity >= :quantity")
    int decreaseStockQuantity(@Param("idVariant") Integer idVariant, @Param("quantity") Integer quantity);

    // hoàn lại tồn kho khi hủy đơn hoặc tăng tồn kho khi nhập hàng
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity + :quantity " +
           "WHERE v.idVariant = :idVariant")
    int increaseStockQuantity(@Param("idVariant") Integer idVariant, @Param("quantity") Integer quantity);

    // cập nhật giá vốn nhập của biến thể khi nhập hàng
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.originalPrice = :originalPrice WHERE v.idVariant = :idVariant")
    int updateOriginalPrice(@Param("idVariant") Integer idVariant, @Param("originalPrice") BigDecimal originalPrice);
}
