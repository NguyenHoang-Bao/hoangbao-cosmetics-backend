package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.CategoryRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Integer id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Integer id, CategoryRequest request);

    void deleteCategory(Integer id);
}
