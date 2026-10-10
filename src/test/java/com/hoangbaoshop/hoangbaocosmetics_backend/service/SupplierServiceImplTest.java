package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.SupplierRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.SupplierResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Supplier;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ImportReceiptRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ImportReceiptRepository importReceiptRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void testGetSupplierById_Success() {
        Supplier supplier = Supplier.builder()
                .idSupplier(1)
                .name("Công ty TNHH Phân phối ABC")
                .phone("0901234567")
                .address("Hà Nội")
                .build();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        SupplierResponse res = supplierService.getSupplierById(1);

        assertNotNull(res);
        assertEquals(1, res.getIdSupplier());
        assertEquals("Công ty TNHH Phân phối ABC", res.getName());
        assertEquals("0901234567", res.getPhone());
    }

    @Test
    void testGetSupplierById_NotFound_ThrowsException() {
        when(supplierRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.getSupplierById(99));
    }

    @Test
    void testCreateSupplier_Success() {
        SupplierRequest req = SupplierRequest.builder()
                .name("Nhà phân phối XYZ")
                .phone("0987654321")
                .address("TP.HCM")
                .build();
        when(supplierRepository.existsByName("Nhà phân phối XYZ")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setIdSupplier(5);
            return s;
        });

        SupplierResponse res = supplierService.createSupplier(req);

        assertNotNull(res);
        assertEquals(5, res.getIdSupplier());
        assertEquals("Nhà phân phối XYZ", res.getName());
    }

    @Test
    void testCreateSupplier_DuplicateName_ThrowsBadRequestException() {
        SupplierRequest req = SupplierRequest.builder()
                .name("Nhà phân phối XYZ")
                .build();
        when(supplierRepository.existsByName("Nhà phân phối XYZ")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> supplierService.createSupplier(req));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testUpdateSupplier_Success() {
        Supplier existing = Supplier.builder()
                .idSupplier(1)
                .name("Tên cũ")
                .phone("0111111111")
                .address("Cũ")
                .build();
        SupplierRequest req = SupplierRequest.builder()
                .name("Tên mới")
                .phone("0222222222")
                .address("Mới")
                .build();

        when(supplierRepository.findById(1)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByName("Tên mới")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierResponse res = supplierService.updateSupplier(1, req);

        assertNotNull(res);
        assertEquals("Tên mới", res.getName());
        assertEquals("0222222222", res.getPhone());
    }

    @Test
    void testUpdateSupplier_DuplicateName_ThrowsBadRequestException() {
        Supplier existing = Supplier.builder()
                .idSupplier(1)
                .name("Tên cũ")
                .build();
        SupplierRequest req = SupplierRequest.builder()
                .name("Tên trùng")
                .build();

        when(supplierRepository.findById(1)).thenReturn(Optional.of(existing));
        when(supplierRepository.existsByName("Tên trùng")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> supplierService.updateSupplier(1, req));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testDeleteSupplier_HasLinkedReceipts_ThrowsBadRequestException() {
        Supplier supplier = Supplier.builder().idSupplier(1).build();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(importReceiptRepository.existsBySupplierIdSupplier(1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> supplierService.deleteSupplier(1));
        verify(supplierRepository, never()).delete(any());
    }

    @Test
    void testDeleteSupplier_Success() {
        Supplier supplier = Supplier.builder().idSupplier(1).build();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(importReceiptRepository.existsBySupplierIdSupplier(1)).thenReturn(false);

        supplierService.deleteSupplier(1);

        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    void testGetAllSuppliers_PagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Supplier s = Supplier.builder().idSupplier(1).name("ABC").build();
        PageImpl<Supplier> page = new PageImpl<>(Collections.singletonList(s), pageable, 1);

        when(supplierRepository.searchSuppliers("ABC", pageable)).thenReturn(page);

        PageResponse<SupplierResponse> res = supplierService.getAllSuppliers("ABC", pageable);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals(1, res.getContent().size());
        assertEquals("ABC", res.getContent().get(0).getName());
    }
}
