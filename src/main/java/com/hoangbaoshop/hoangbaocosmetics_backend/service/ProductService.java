package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.ProductRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(String keyword, Integer categoryId, Integer brandId, Pageable pageable);

    ProductResponse getProductById(Integer id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Integer id, ProductRequest request);

    void deleteProduct(Integer id);
}