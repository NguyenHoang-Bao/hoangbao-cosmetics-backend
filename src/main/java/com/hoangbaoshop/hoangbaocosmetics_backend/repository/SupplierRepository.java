package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    // tìm kiếm nhà cung cấp theo keyword (tên, sđt, địa chỉ) có phân trang
    @Query("SELECT s FROM Supplier s WHERE :keyword IS NULL OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "s.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Supplier> searchSuppliers(@Param("keyword") String keyword, Pageable pageable);

    // kiểm tra nhà cung cấp đã tồn tại theo tên
    boolean existsByName(String name);
}
