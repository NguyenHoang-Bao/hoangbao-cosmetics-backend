package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.CategoryRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Category Management", description = "Các API truy vấn và quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả danh mục (Public)", description = "Dành cho Frontend hiển thị menu, navbar và bộ lọc danh mục")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết danh mục theo ID (Public)")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm danh mục mới (Admin)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Tạo danh mục thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin danh mục (Admin)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request), "Cập nhật danh mục thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Xóa danh mục thành công"));
    }
}
