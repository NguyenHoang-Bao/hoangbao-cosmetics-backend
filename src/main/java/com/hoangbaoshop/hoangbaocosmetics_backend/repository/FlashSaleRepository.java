package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FlashSale;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.FlashSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {

    @Query("SELECT DISTINCT fs FROM FlashSale fs " +
           "LEFT JOIN FETCH fs.items item " +
           "LEFT JOIN FETCH item.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE fs.status = :status AND :now >= fs.startTime AND :now <= fs.endTime " +
           "ORDER BY fs.endTime ASC")
    List<FlashSale> findActiveFlashSalesWithDetails(@Param("status") FlashSaleStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT fs FROM FlashSale fs " +
           "LEFT JOIN FETCH fs.items item " +
           "LEFT JOIN FETCH item.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "WHERE fs.idFlashSale = :id")
    Optional<FlashSale> findByIdWithDetails(@Param("id") Integer id);
}
