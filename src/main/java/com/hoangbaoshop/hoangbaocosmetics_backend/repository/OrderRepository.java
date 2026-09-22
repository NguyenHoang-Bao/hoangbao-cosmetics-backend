package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUserUsername(String username, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:orderType IS NULL OR o.orderType = :orderType) AND " +
           "(:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " o.phoneNumber LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchOrdersAdmin(
            @Param("status") OrderStatus status,
            @Param("orderType") OrderType orderType,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED")
    Double sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double sumRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByStatus(OrderStatus status);

    @Query("SELECT FUNCTION('DATE', o.createdAt), COALESCE(SUM(o.totalAmount), 0.0), COUNT(o) " +
           "FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    java.util.List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('MONTH', o.createdAt), COALESCE(SUM(o.totalAmount), 0.0), COUNT(o) " +
           "FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "AND FUNCTION('YEAR', o.createdAt) = :year " +
           "GROUP BY FUNCTION('MONTH', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt) ASC")
    java.util.List<Object[]> getMonthlyRevenue(@Param("year") int year);
}
