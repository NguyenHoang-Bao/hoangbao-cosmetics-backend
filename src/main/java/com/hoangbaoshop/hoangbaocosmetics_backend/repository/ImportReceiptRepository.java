package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ImportReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Integer> {

    @Query("SELECT r FROM ImportReceipt r WHERE " +
           "(:supplierId IS NULL OR r.supplier.idSupplier = :supplierId) AND " +
           "(:startDate IS NULL OR r.importDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.importDate <= :endDate)")
    Page<ImportReceipt> searchImportReceipts(
            @Param("supplierId") Integer supplierId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    boolean existsBySupplierIdSupplier(Integer supplierId);
}
