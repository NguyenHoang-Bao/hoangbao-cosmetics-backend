package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user bằng username khi gõ vào form đăng nhập
    Optional<User> findByUsername(String username);

    // Tìm user bằng email (dùng cho đăng nhập Google OAuth và quên mật khẩu)
    Optional<User> findByEmail(String email);
    // Tìm khách hàng bằng số điện thoại để tích điểm & POS
    Optional<User> findByPhoneNumber(String phoneNumber);

    // check trùng khi đăng ký
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    long countByRolesNameRole(String nameRole);
}