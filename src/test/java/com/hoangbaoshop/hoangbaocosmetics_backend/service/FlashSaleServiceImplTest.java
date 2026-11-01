package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale.FlashSaleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.FlashSaleStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FlashSaleItemRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FlashSaleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashSaleServiceImplTest {

    @Mock
    private FlashSaleRepository flashSaleRepository;
    @Mock
    private FlashSaleItemRepository flashSaleItemRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private FlashSaleServiceImpl flashSaleService;

    @Test
    void testGetActiveFlashSale_WhenActive_ReturnsResponseWithItems() {
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder().idProduct(1).name("Kem Dưỡng").thumbnail("img.jpg").build();
        ProductVariant variant = ProductVariant.builder()
                .idVariant(1)
                .product(product)
                .sku("SKU-1")
                .originalPrice(new BigDecimal("500000"))
                .price(new BigDecimal("400000"))
                .build();

        FlashSaleItem item = FlashSaleItem.builder()
                .idItem(1)
                .variant(variant)
                .salePrice(new BigDecimal("350000"))
                .stockQuantity(100)
                .soldQuantity(50)
                .build();

        FlashSale flashSale = FlashSale.builder()
                .idFlashSale(1)
                .title("Flash Sale Cuối Tuần")
                .startTime(now.minusHours(1))
                .endTime(now.plusDays(2))
                .status(FlashSaleStatus.ACTIVE)
                .items(Collections.singletonList(item))
                .build();

        when(flashSaleRepository.findActiveFlashSalesWithDetails(eq(FlashSaleStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(flashSale));

        FlashSaleResponse res = flashSaleService.getActiveFlashSale();

        assertNotNull(res);
        assertEquals("Flash Sale Cuối Tuần", res.getTitle());
        assertEquals(FlashSaleStatus.ACTIVE, res.getStatus());
        assertEquals(1, res.getItems().size());
        assertEquals(new BigDecimal("350000"), res.getItems().get(0).getSalePrice());
        assertEquals(50, res.getItems().get(0).getSoldPercentage());
    }

    @Test
    void testGetActiveFlashSale_WhenNoneActive_ReturnsEndedResponse() {
        when(flashSaleRepository.findActiveFlashSalesWithDetails(eq(FlashSaleStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        FlashSaleResponse res = flashSaleService.getActiveFlashSale();

        assertNotNull(res);
        assertEquals(FlashSaleStatus.ENDED, res.getStatus());
        assertTrue(res.getItems().isEmpty());
    }

    @Test
    void testGetFlashSaleById_Success() {
        FlashSale flashSale = FlashSale.builder()
                .idFlashSale(10)
                .title("Super Sale")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(1))
                .status(FlashSaleStatus.ACTIVE)
                .build();

        when(flashSaleRepository.findByIdWithDetails(10)).thenReturn(Optional.of(flashSale));

        FlashSaleResponse res = flashSaleService.getFlashSaleById(10);

        assertNotNull(res);
        assertEquals(10, res.getIdFlashSale());
        assertEquals("Super Sale", res.getTitle());
    }

    @Test
    void testGetFlashSaleById_NotFound_ThrowsException() {
        when(flashSaleRepository.findByIdWithDetails(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> flashSaleService.getFlashSaleById(999));
    }

    @Test
    void testCreateFlashSale_Success() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(3);

        Product product = Product.builder().idProduct(2).name("Serum").build();
        ProductVariant variant = ProductVariant.builder().idVariant(5).product(product).build();

        FlashSaleItemRequest itemReq = FlashSaleItemRequest.builder()
                .idVariant(5)
                .salePrice(new BigDecimal("200000"))
                .stockQuantity(50)
                .build();

        FlashSaleRequest req = FlashSaleRequest.builder()
                .title("Sale Tháng 11")
                .startTime(start)
                .endTime(end)
                .items(Collections.singletonList(itemReq))
                .build();

        when(productVariantRepository.findByIdWithProduct(5)).thenReturn(Optional.of(variant));
        when(flashSaleRepository.save(any(FlashSale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlashSaleResponse res = flashSaleService.createFlashSale(req);

        assertNotNull(res);
        assertEquals("Sale Tháng 11", res.getTitle());
        assertEquals(1, res.getItems().size());
        assertEquals(new BigDecimal("200000"), res.getItems().get(0).getSalePrice());
    }

    @Test
    void testCreateFlashSale_StartTimeAfterEndTime_ThrowsBadRequest() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        FlashSaleRequest req = FlashSaleRequest.builder()
                .title("Invalid Sale")
                .startTime(start)
                .endTime(end)
                .items(Collections.emptyList())
                .build();

        assertThrows(BadRequestException.class, () -> flashSaleService.createFlashSale(req));
        verify(flashSaleRepository, never()).save(any());
    }
}
