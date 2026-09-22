package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT v.product.idProduct, v.product.name, v.product.thumbnail, v.product.category.name, " +
           "SUM(d.quantity), SUM(d.price * d.quantity) " +
           "FROM OrderDetail d " +
           "JOIN d.variant v " +
           "JOIN d.order o " +
           "WHERE o.status != com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus.CANCELLED " +
           "GROUP BY v.product.idProduct, v.product.name, v.product.thumbnail, v.product.category.name " +
           "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
