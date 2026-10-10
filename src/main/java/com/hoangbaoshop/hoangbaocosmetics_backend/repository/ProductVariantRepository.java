package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    Optional<ProductVariant> findBySku(String sku);

    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity <= :threshold ORDER BY v.stockQuantity ASC")
    List<ProductVariant> findLowStockVariants(@Param("threshold") int threshold);

    long countByStockQuantityLessThanEqual(int threshold);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.idVariant = :idVariant")
    Optional<ProductVariant> findByIdWithProduct(@Param("idVariant") Integer idVariant);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product WHERE v.idVariant IN :ids")
    List<ProductVariant> findAllWithProductByIdIn(@Param("ids") Collection<Integer> ids);

    /**
     * Trừ tồn kho một cách an toàn (Atomic Update ở mức Database).
     * Chỉ trừ nếu số lượng tồn kho hiện tại (stockQuantity) >= quantity cần mua.
     * Trả về số dòng cập nhật thành công (1 nếu thành công, 0 nếu không đủ hàng).
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity - :quantity " +
           "WHERE v.idVariant = :idVariant AND v.stockQuantity >= :quantity")
    int decreaseStockQuantity(@Param("idVariant") Integer idVariant, @Param("quantity") Integer quantity);

    /**
     * Hoàn lại tồn kho khi đơn hàng bị hủy hoặc tăng tồn kho khi nhập hàng.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.stockQuantity = v.stockQuantity + :quantity " +
           "WHERE v.idVariant = :idVariant")
    int increaseStockQuantity(@Param("idVariant") Integer idVariant, @Param("quantity") Integer quantity);

    /**
     * Cập nhật giá gốc (giá vốn nhập) của biến thể khi nhập hàng.
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.originalPrice = :originalPrice WHERE v.idVariant = :idVariant")
    int updateOriginalPrice(@Param("idVariant") Integer idVariant, @Param("originalPrice") Double originalPrice);
}
