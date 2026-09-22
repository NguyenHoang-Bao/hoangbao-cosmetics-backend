package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.BrandRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Brand;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.BrandRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        return mapToResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại: " + request.getName());
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .logoUrl(request.getLogoUrl() != null ? request.getLogoUrl().trim() : null)
                .build();

        Brand saved = brandRepository.save(brand);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Integer id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        if (!brand.getName().equalsIgnoreCase(request.getName().trim()) 
                && brandRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên thương hiệu đã tồn tại: " + request.getName());
        }

        brand.setName(request.getName().trim());
        if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl().trim());
        }

        Brand updated = brandRepository.save(brand);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        if (productRepository.existsByBrand_IdBrand(id)) {
            throw new BadRequestException("Không thể xóa thương hiệu đang có sản phẩm liên kết!");
        }

        brandRepository.delete(brand);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .idBrand(brand.getIdBrand())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .build();
    }
}
