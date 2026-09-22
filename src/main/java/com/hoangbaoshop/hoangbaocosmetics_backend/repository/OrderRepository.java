package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUserUsername(String username, Pageable pageable);
}
