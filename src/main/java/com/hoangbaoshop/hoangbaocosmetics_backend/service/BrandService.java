package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.BrandRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;

import java.util.List;

public interface BrandService {
    // lấy all danh sách brand
    List<BrandResponse> getAllBrands();
    // lấy 1 brand by id
    BrandResponse getBrandById(Integer id);
    // tạo Brand mới
    BrandResponse createBrand(BrandRequest request);
    // sửa brand
    BrandResponse updateBrand(Integer id, BrandRequest request);
    // xóa brand
    void deleteBrand(Integer id);
}
