package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.ProductRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // lấy danh sách sản phẩm (tìm kiếm theo từ khóa, danh mục, thương hiệu & phân trang)
    PageResponse<ProductResponse> getAllProducts(String keyword, Integer categoryId, Integer brandId, Pageable pageable);

    // lấy chi tiết sản phẩm theo id (kèm danh sách biến thể & ảnh)
    ProductResponse getProductById(Integer id);

    // tạo sản phẩm mới (kèm biến thể & ảnh)
    ProductResponse createProduct(ProductRequest request);

    // cập nhật thông tin sản phẩm
    ProductResponse updateProduct(Integer id, ProductRequest request);

    // xóa sản phẩm (soft delete ẩn sản phẩm)
    void deleteProduct(Integer id);
}