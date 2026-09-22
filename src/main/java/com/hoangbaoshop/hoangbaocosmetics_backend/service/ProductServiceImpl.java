package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.ProductRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.ProductResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.ProductVariantResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.BrandRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CategoryRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(String keyword, Integer categoryId, Integer brandId, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(keyword, categoryId, brandId, pageable);
        List<ProductResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getIdCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getIdCategory()));

        Brand brand = brandRepository.findById(request.getIdBrand())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + request.getIdBrand()));

        Product product = Product.builder()
                .name(request.getName())
                .category(category)
                .brand(brand)
                .description(request.getDescription())
                .benefits(request.getBenefits())
                .keyIngredients(request.getKeyIngredients())
                .usageInstruction(request.getUsageInstruction())
                .thumbnail(request.getThumbnail())
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        if (request.getVariants() != null) {
            List<ProductVariant> variants = request.getVariants().stream()
                    .map(v -> ProductVariant.builder()
                            .product(product)
                            .sku(v.getSku())
                            .capacity(v.getCapacity())
                            .originalPrice(v.getOriginalPrice())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .build())
                    .collect(Collectors.toList());
            product.setVariants(variants);
        }

        if (request.getImageUrls() != null) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .product(product)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        Category category = categoryRepository.findById(request.getIdCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getIdCategory()));

        Brand brand = brandRepository.findById(request.getIdBrand())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + request.getIdBrand()));

        product.setName(request.getName());
        product.setCategory(category);
        product.setBrand(brand);
        product.setDescription(request.getDescription());
        product.setBenefits(request.getBenefits());
        product.setKeyIngredients(request.getKeyIngredients());
        product.setUsageInstruction(request.getUsageInstruction());
        product.setThumbnail(request.getThumbnail());

        // Cập nhật variants
        if (request.getVariants() != null) {
            product.getVariants().clear();
            request.getVariants().forEach(v -> product.getVariants().add(
                    ProductVariant.builder()
                            .product(product)
                            .sku(v.getSku())
                            .capacity(v.getCapacity())
                            .originalPrice(v.getOriginalPrice())
                            .price(v.getPrice())
                            .stockQuantity(v.getStockQuantity())
                            .build()
            ));
        }

        // Cập nhật ảnh phụ
        if (request.getImageUrls() != null) {
            product.getImages().clear();
            request.getImageUrls().forEach(url -> product.getImages().add(
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(url)
                            .build()
            ));
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .idCategory(product.getCategory().getIdCategory())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .build();
        }

        BrandResponse brandResponse = null;
        if (product.getBrand() != null) {
            brandResponse = BrandResponse.builder()
                    .idBrand(product.getBrand().getIdBrand())
                    .name(product.getBrand().getName())
                    .logoUrl(product.getBrand().getLogoUrl())
                    .build();
        }

        List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                .map(v -> ProductVariantResponse.builder()
                        .idVariant(v.getIdVariant())
                        .sku(v.getSku())
                        .capacity(v.getCapacity())
                        .originalPrice(v.getOriginalPrice())
                        .price(v.getPrice())
                        .stockQuantity(v.getStockQuantity())
                        .build())
                .collect(Collectors.toList());

        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .idProduct(product.getIdProduct())
                .name(product.getName())
                .category(categoryResponse)
                .brand(brandResponse)
                .description(product.getDescription())
                .benefits(product.getBenefits())
                .keyIngredients(product.getKeyIngredients())
                .usageInstruction(product.getUsageInstruction())
                .thumbnail(product.getThumbnail())
                .variants(variantResponses)
                .images(imageUrls)
                .build();
    }
}