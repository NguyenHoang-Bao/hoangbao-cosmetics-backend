package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // tìm đơn hàng theo mã đơn hàng
    Optional<Order> findByOrderCode(String orderCode);

    // khóa dòng đơn hàng (pessimistic write lock) khi xử lý webhook thanh toán
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCodeForUpdate(@Param("orderCode") String orderCode);

    // khóa dòng đơn hàng theo ID (pessimistic write lock) khi admin thao tác hủy đơn
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.idOrder = :idOrder")
    Optional<Order> findByIdForUpdate(@Param("idOrder") Integer idOrder);

    // cập nhật trạng thái đơn hàng nguyên tử (chỉ thành công nếu trạng thái hiện tại đúng như kỳ vọng)
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus, o.note = :note, o.updatedAt = :now, o.version = o.version + 1 " +
           "WHERE o.idOrder = :idOrder AND o.status = :expectedStatus")
    int updateStatusIfMatches(
            @Param("idOrder") Integer idOrder,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("newStatus") OrderStatus newStatus,
            @Param("note") String note,
            @Param("now") LocalDateTime now
    );

    // kiểm tra mã giao dịch thanh toán đã tồn tại chưa
    boolean existsByTransactionId(String transactionId);

    // lấy danh sách đơn hàng của user có phân trang
    Page<Order> findByUserUsername(String username, Pageable pageable);

    // tìm các đơn hàng theo trạng thái, phương thức thanh toán và thời gian tạo trước một mốc (phục vụ quét hủy đơn quá hạn)
    java.util.List<Order> findByStatusAndPaymentMethodInAndCreatedAtBefore(
            OrderStatus status, java.util.List<com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod> methods, LocalDateTime before);

    // tìm kiếm đơn hàng cho admin/staff kèm bộ lọc và phân trang
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

    // tính tổng doanh thu toàn thời gian (trừ đơn hủy)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED")
    BigDecimal sumTotalRevenue();

    // tính tổng doanh thu trong khoảng thời gian (trừ đơn hủy)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // đếm số lượng đơn hàng theo trạng thái cụ thể
    long countByStatus(OrderStatus status);

    // đếm số lượng đơn hàng theo từng trạng thái (phục vụ biểu đồ tròn)
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    java.util.List<Object[]> countOrdersByStatusGrouped();

    // thống kê doanh thu theo ngày trong khoảng thời gian
    @Query("SELECT FUNCTION('DATE', o.createdAt), COALESCE(SUM(o.totalAmount), 0.0), COUNT(o) " +
           "FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    java.util.List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // thống kê doanh thu theo tháng trong khoảng thời gian
    @Query("SELECT FUNCTION('MONTH', o.createdAt), COALESCE(SUM(o.totalAmount), 0.0), COUNT(o) " +
           "FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "AND o.createdAt >= :startDate AND o.createdAt < :endDate " +
           "GROUP BY FUNCTION('MONTH', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt) ASC")
    java.util.List<Object[]> getMonthlyRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // thống kê doanh thu 12 tháng của năm
    @Query("SELECT FUNCTION('MONTH', o.createdAt), COALESCE(SUM(o.totalAmount), 0.0), COUNT(o) " +
           "FROM Order o WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "AND FUNCTION('YEAR', o.createdAt) = :year " +
           "GROUP BY FUNCTION('MONTH', o.createdAt) ORDER BY FUNCTION('MONTH', o.createdAt) ASC")
    java.util.List<Object[]> getMonthlyRevenue(@Param("year") int year);
}

