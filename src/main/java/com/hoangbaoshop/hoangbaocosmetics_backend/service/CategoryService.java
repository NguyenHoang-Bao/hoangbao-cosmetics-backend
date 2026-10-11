package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.CategoryRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;

import java.util.List;

public interface CategoryService {

    // lấy all danh sách category
    List<CategoryResponse> getAllCategories();

    // lấy 1 category by id
    CategoryResponse getCategoryById(Integer id);

    // tạo category mới
    CategoryResponse createCategory(CategoryRequest request);

    // sửa category
    CategoryResponse updateCategory(Integer id, CategoryRequest request);

    // xóa category
    void deleteCategory(Integer id);
}