package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.CreateImportReceiptRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.ImportReceiptItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ImportReceipt;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Supplier;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private ImportReceiptRepository importReceiptRepository;
    @Mock
    private ImportReceiptDetailRepository importReceiptDetailRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    @Test
    void testCreateImportReceipt_UpdatesStockAndPriceAtomically() {
        // Given
        CreateImportReceiptRequest request = CreateImportReceiptRequest.builder()
                .idSupplier(1)
                .note("Nhập kho lô hàng mới")
                .items(Collections.singletonList(
                        ImportReceiptItemRequest.builder()
                                .idVariant(10)
                                .quantity(50)
                                .importPrice(120000.0)
                                .build()
                ))
                .build();

        Supplier supplier = Supplier.builder().idSupplier(1).name("Nhà cung cấp A").build();
        User manager = User.builder().idUser(2).fullName("Thủ kho Nguyễn").build();
        ProductVariant variant = ProductVariant.builder()
                .idVariant(10)
                .sku("SKU-10")
                .stockQuantity(100)
                .originalPrice(100000.0)
                .build();

        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(importReceiptRepository.save(any(ImportReceipt.class))).thenAnswer(i -> {
            ImportReceipt r = i.getArgument(0);
            r.setIdReceipt(100);
            return r;
        });
        when(productVariantRepository.findById(10)).thenReturn(Optional.of(variant));

        // When
        ImportReceiptResponse response = warehouseService.createImportReceipt("manager", request);

        // Then
        assertNotNull(response);
        // Verify atomic update methods are called on database level
        verify(productVariantRepository).increaseStockQuantity(10, 50);
        verify(productVariantRepository).updateOriginalPrice(10, 120000.0);
        // Verify atomic updates replaced in-memory setStockQuantity + save
        verify(productVariantRepository, never()).save(variant);
    }
}
