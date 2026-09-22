package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.InventoryLog;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {

    @Query("SELECT l FROM InventoryLog l WHERE " +
           "(:variantId IS NULL OR l.variant.idVariant = :variantId) AND " +
           "(:transactionType IS NULL OR l.transactionType = :transactionType) AND " +
           "(:startDate IS NULL OR l.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR l.createdAt <= :endDate)")
    Page<InventoryLog> searchLogs(
            @Param("variantId") Integer variantId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
