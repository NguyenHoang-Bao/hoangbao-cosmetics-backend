package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // tìm user bằng username khi đăng nhập
    Optional<User> findByUsername(String username);

    // tìm user bằng email (dùng cho Google OAuth và quên mật khẩu)
    Optional<User> findByEmail(String email);

    // tìm khách hàng bằng số điện thoại để tích điểm & POS
    Optional<User> findByPhoneNumber(String phoneNumber);

    // kiểm tra trùng lặp thông tin khi đăng ký
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // đếm số lượng người dùng theo vai trò (role)
    long countByRolesNameRole(String nameRole);
}