package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale.FlashSaleItemResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale.FlashSaleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FlashSale;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FlashSaleItem;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.FlashSaleStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FlashSaleItemRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FlashSaleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "flash_sales", key = "'active'")
    public FlashSaleResponse getActiveFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeSales = flashSaleRepository.findActiveFlashSalesWithDetails(FlashSaleStatus.ACTIVE, now);

        if (activeSales.isEmpty()) {
            return FlashSaleResponse.builder()
                    .title("Không có đợt Flash Sale đang diễn ra")
                    .status(FlashSaleStatus.ENDED)
                    .remainingSeconds(0L)
                    .items(Collections.emptyList())
                    .build();
        }

        FlashSale currentSale = activeSales.get(0);
        return mapToResponse(currentSale, now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getAllFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        return flashSaleRepository.findAll().stream()
                .map(fs -> mapToResponse(fs, now))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FlashSaleResponse getFlashSaleById(Integer id) {
        FlashSale flashSale = flashSaleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt Flash Sale với ID: " + id));
        return mapToResponse(flashSale, LocalDateTime.now());
    }

    @Override
    @Transactional
    @CacheEvict(value = "flash_sales", allEntries = true)
    public FlashSaleResponse createFlashSale(FlashSaleRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Thời gian bắt đầu không thể sau thời gian kết thúc!");
        }

        FlashSale flashSale = FlashSale.builder()
                .title(request.getTitle().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(FlashSaleStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();

        for (FlashSaleItemRequest itemReq : request.getItems()) {
            ProductVariant variant = productVariantRepository.findByIdWithProduct(itemReq.getIdVariant())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + itemReq.getIdVariant()));

            FlashSaleItem item = FlashSaleItem.builder()
                    .flashSale(flashSale)
                    .variant(variant)
                    .salePrice(itemReq.getSalePrice())
                    .stockQuantity(itemReq.getStockQuantity())
                    .soldQuantity(0)
                    .build();

            flashSale.getItems().add(item);
        }

        FlashSale saved = flashSaleRepository.save(flashSale);
        return mapToResponse(saved, LocalDateTime.now());
    }

    @Override
    @Transactional
    @CacheEvict(value = "flash_sales", allEntries = true)
    public void deleteFlashSale(Integer id) {
        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt Flash Sale với ID: " + id));
        flashSaleRepository.delete(flashSale);
    }

    // Helper mapping Entity -> Response DTO
    private FlashSaleResponse mapToResponse(FlashSale flashSale, LocalDateTime now) {
        long remainingSeconds = 0L;
        if (flashSale.getEndTime() != null && flashSale.getEndTime().isAfter(now)) {
            remainingSeconds = Math.max(0, Duration.between(now, flashSale.getEndTime()).getSeconds());
        }

        List<FlashSaleItemResponse> itemResponses = (flashSale.getItems() == null)
                ? Collections.emptyList()
                : flashSale.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return FlashSaleResponse.builder()
                .idFlashSale(flashSale.getIdFlashSale())
                .title(flashSale.getTitle())
                .startTime(flashSale.getStartTime())
                .endTime(flashSale.getEndTime())
                .status(flashSale.getStatus())
                .remainingSeconds(remainingSeconds)
                .items(itemResponses)
                .build();
    }

    private FlashSaleItemResponse mapItemToResponse(FlashSaleItem item) {
        ProductVariant variant = item.getVariant();
        Product product = (variant != null) ? variant.getProduct() : null;

        BigDecimal originalPrice = (variant != null && variant.getOriginalPrice() != null)
                ? variant.getOriginalPrice()
                : BigDecimal.ZERO;
        BigDecimal regularPrice = (variant != null && variant.getPrice() != null)
                ? variant.getPrice()
                : BigDecimal.ZERO;
        BigDecimal salePrice = (item.getSalePrice() != null)
                ? item.getSalePrice()
                : regularPrice;

        // Tính % giảm giá dựa trên giá gốc ban đầu hoặc giá bán thường
        int discountPercent = 0;
        BigDecimal basePriceForDiscount = originalPrice.compareTo(BigDecimal.ZERO) > 0 ? originalPrice : regularPrice;
        if (basePriceForDiscount.compareTo(BigDecimal.ZERO) > 0 && basePriceForDiscount.compareTo(salePrice) > 0) {
            BigDecimal diff = basePriceForDiscount.subtract(salePrice);
            discountPercent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(basePriceForDiscount, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        // Tính % số lượng đã bán
        int stockQuantity = (item.getStockQuantity() != null) ? item.getStockQuantity() : 0;
        int soldQuantity = (item.getSoldQuantity() != null) ? item.getSoldQuantity() : 0;
        int soldPercentage = 0;
        if (stockQuantity > 0) {
            soldPercentage = Math.min(100, (int) Math.round(((double) soldQuantity / stockQuantity) * 100));
        }

        boolean isSoldOut = (stockQuantity > 0 && soldQuantity >= stockQuantity);

        return FlashSaleItemResponse.builder()
                .idItem(item.getIdItem())
                .idProduct((product != null) ? product.getIdProduct() : null)
                .productName((product != null) ? product.getName() : "Sản phẩm")
                .thumbnail((product != null) ? product.getThumbnail() : null)
                .categoryName((product != null && product.getCategory() != null) ? product.getCategory().getName() : null)
                .brandName((product != null && product.getBrand() != null) ? product.getBrand().getName() : null)
                .idVariant((variant != null) ? variant.getIdVariant() : null)
                .sku((variant != null) ? variant.getSku() : null)
                .capacity((variant != null) ? variant.getCapacity() : null)
                .originalPrice(originalPrice)
                .regularPrice(regularPrice)
                .salePrice(salePrice)
                .discountPercent(discountPercent)
                .stockQuantity(stockQuantity)
                .soldQuantity(soldQuantity)
                .soldPercentage(soldPercentage)
                .isSoldOut(isSoldOut)
                .build();
    }
}
