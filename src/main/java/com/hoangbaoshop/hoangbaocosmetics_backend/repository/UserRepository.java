package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    //tim user khi dang nhap
    Optional<User> findByUsername(string)
}
