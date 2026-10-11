package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // tìm danh mục theo slug
    Optional<Category> findBySlug(String slug);

    // kiểm tra danh mục đã tồn tại theo tên
    boolean existsByName(String name);

    // kiểm tra danh mục đã tồn tại theo slug
    boolean existsBySlug(String slug);
}