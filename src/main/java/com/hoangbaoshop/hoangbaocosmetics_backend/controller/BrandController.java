package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.BrandRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.BrandService;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Brand Management", description = "Các API truy vấn và quản lý thương hiệu sản phẩm")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả thương hiệu (Public)", description = "Dành cho Frontend hiển thị carousel thương hiệu và bộ lọc")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAllBrands()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết thương hiệu theo ID (Public)")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm thương hiệu mới (Admin)")
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Tạo thương hiệu thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thương hiệu (Admin)")
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @PathVariable Integer id,
            @Valid @RequestBody BrandRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(brandService.updateBrand(id, request), "Cập nhật thương hiệu thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa thương hiệu (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.<Void>success(null, "Xóa thương hiệu thành công"));
    }
}
