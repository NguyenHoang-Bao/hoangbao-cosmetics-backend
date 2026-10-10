package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.BrandRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.BrandResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Brand;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.BrandRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    @Test
    void testGetAllBrands_ReturnsList() {
        Brand brand = Brand.builder().idBrand(1).name("Innisfree").logoUrl("logo.png").build();
        when(brandRepository.findAll()).thenReturn(Collections.singletonList(brand));

        List<BrandResponse> list = brandService.getAllBrands();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Innisfree", list.get(0).getName());
        assertEquals("logo.png", list.get(0).getLogoUrl());
    }

    @Test
    void testGetBrandById_Success() {
        Brand brand = Brand.builder().idBrand(1).name("Innisfree").build();
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));

        BrandResponse res = brandService.getBrandById(1);

        assertNotNull(res);
        assertEquals(1, res.getIdBrand());
        assertEquals("Innisfree", res.getName());
    }

    @Test
    void testGetBrandById_NotFound_ThrowsException() {
        when(brandRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.getBrandById(99));
    }

    @Test
    void testCreateBrand_Success() {
        BrandRequest req = BrandRequest.builder().name("La Roche-Posay").logoUrl("lrp.png").build();
        when(brandRepository.existsByName("La Roche-Posay")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand b = invocation.getArgument(0);
            b.setIdBrand(10);
            return b;
        });

        BrandResponse res = brandService.createBrand(req);

        assertNotNull(res);
        assertEquals(10, res.getIdBrand());
        assertEquals("La Roche-Posay", res.getName());
        assertEquals("lrp.png", res.getLogoUrl());
    }

    @Test
    void testCreateBrand_DuplicateName_ThrowsBadRequestException() {
        BrandRequest req = BrandRequest.builder().name("Innisfree").build();
        when(brandRepository.existsByName("Innisfree")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> brandService.createBrand(req));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void testUpdateBrand_Success() {
        Brand existing = Brand.builder().idBrand(1).name("Old Name").logoUrl("old.png").build();
        BrandRequest req = BrandRequest.builder().name("New Name").logoUrl("new.png").build();

        when(brandRepository.findById(1)).thenReturn(Optional.of(existing));
        when(brandRepository.existsByName("New Name")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse res = brandService.updateBrand(1, req);

        assertNotNull(res);
        assertEquals("New Name", res.getName());
        assertEquals("new.png", res.getLogoUrl());
    }

    @Test
    void testUpdateBrand_DuplicateName_ThrowsBadRequestException() {
        Brand existing = Brand.builder().idBrand(1).name("Old Name").build();
        BrandRequest req = BrandRequest.builder().name("Existing Brand").build();

        when(brandRepository.findById(1)).thenReturn(Optional.of(existing));
        when(brandRepository.existsByName("Existing Brand")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> brandService.updateBrand(1, req));
        verify(brandRepository, never()).save(any());
    }

    @Test
    void testDeleteBrand_HasLinkedProducts_ThrowsBadRequestException() {
        Brand brand = Brand.builder().idBrand(1).build();
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        when(productRepository.existsByBrand_IdBrand(1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> brandService.deleteBrand(1));
        verify(brandRepository, never()).delete(any());
    }

    @Test
    void testDeleteBrand_Success() {
        Brand brand = Brand.builder().idBrand(1).build();
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        when(productRepository.existsByBrand_IdBrand(1)).thenReturn(false);

        brandService.deleteBrand(1);

        verify(brandRepository, times(1)).delete(brand);
    }
}
