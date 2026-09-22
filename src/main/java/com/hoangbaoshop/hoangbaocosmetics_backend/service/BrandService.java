package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.BrandRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;

import java.util.List;

public interface BrandService {

    List<BrandResponse> getAllBrands();

    BrandResponse getBrandById(Integer id);

    BrandResponse createBrand(BrandRequest request);

    BrandResponse updateBrand(Integer id, BrandRequest request);

    void deleteBrand(Integer id);
}
