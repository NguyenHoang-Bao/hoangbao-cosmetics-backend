package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.CreateImportReceiptRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.ImportReceiptItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptDetailResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.InventoryLogResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportReceiptDetailRepository importReceiptDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional
    public ImportReceiptResponse createImportReceipt(String username, CreateImportReceiptRequest request) {
        User warehouseManager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        Supplier supplier = supplierRepository.findById(request.getIdSupplier())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + request.getIdSupplier()));

        // 1. Tính tổng chi phí nhập hàng
        double totalCost = 0.0;
        for (ImportReceiptItemRequest item : request.getItems()) {
            totalCost += item.getImportPrice() * item.getQuantity();
        }

        // 2. Tạo phiếu nhập kho
        ImportReceipt receipt = ImportReceipt.builder()
                .supplier(supplier)
                .warehouseManager(warehouseManager)
                .totalCost(totalCost)
                .note(request.getNote())
                .details(new ArrayList<>())
                .build();

        ImportReceipt savedReceipt = importReceiptRepository.save(receipt);

        // 3. Xử lý từng sản phẩm nhập: lưu chi tiết, tăng tồn kho và ghi sổ cái kiểm toán
        List<ImportReceiptDetailResponse> detailResponses = new ArrayList<>();

        for (ImportReceiptItemRequest item : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getIdVariant())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getIdVariant()));

            ImportReceiptDetail detail = ImportReceiptDetail.builder()
                    .id(new ImportReceiptDetailId(savedReceipt.getIdReceipt(), variant.getIdVariant()))
                    .importReceipt(savedReceipt)
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .importPrice(item.getImportPrice())
                    .build();

            importReceiptDetailRepository.save(detail);

            // Tự động cộng số lượng tồn kho và cập nhật giá vốn
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variant.setOriginalPrice(item.getImportPrice());
            productVariantRepository.save(variant);

            // Ghi vết vào sổ cái biến động kho (Audit log)
            InventoryLog log = InventoryLog.builder()
                    .variant(variant)
                    .user(warehouseManager)
                    .transactionType(TransactionType.IMPORT)
                    .quantityChange(item.getQuantity())
                    .referenceId(savedReceipt.getIdReceipt())
                    .build();
            inventoryLogRepository.save(log);

            String productName = variant.getProduct() != null ? variant.getProduct().getName() : "";
            detailResponses.add(ImportReceiptDetailResponse.builder()
                    .idVariant(variant.getIdVariant())
                    .sku(variant.getSku())
                    .capacity(variant.getCapacity())
                    .productName(productName)
                    .quantity(item.getQuantity())
                    .importPrice(item.getImportPrice())
                    .subTotal(item.getImportPrice() * item.getQuantity())
                    .build());
        }

        return ImportReceiptResponse.builder()
                .idReceipt(savedReceipt.getIdReceipt())
                .idSupplier(supplier.getIdSupplier())
                .supplierName(supplier.getName())
                .idWarehouseManager(warehouseManager.getIdUser())
                .warehouseManagerName(warehouseManager.getFullName())
                .importDate(savedReceipt.getImportDate())
                .totalCost(savedReceipt.getTotalCost())
                .note(savedReceipt.getNote())
                .items(detailResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ImportReceiptResponse getImportReceiptById(Integer id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        return mapToImportReceiptResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ImportReceiptResponse> getAllImportReceipts(
            Integer supplierId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<ImportReceipt> page = importReceiptRepository.searchImportReceipts(supplierId, startDate, endDate, pageable);
        List<ImportReceiptResponse> content = page.getContent().stream()
                .map(this::mapToImportReceiptResponse)
                .collect(Collectors.toList());

        return PageResponse.<ImportReceiptResponse>builder()
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
    public PageResponse<InventoryLogResponse> getInventoryLogs(
            Integer variantId,
            TransactionType transactionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<InventoryLog> page = inventoryLogRepository.searchLogs(variantId, transactionType, startDate, endDate, pageable);
        List<InventoryLogResponse> content = page.getContent().stream()
                .map(this::mapToInventoryLogResponse)
                .collect(Collectors.toList());

        return PageResponse.<InventoryLogResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ImportReceiptResponse mapToImportReceiptResponse(ImportReceipt receipt) {
        List<ImportReceiptDetail> details = importReceiptDetailRepository.findByIdIdReceipt(receipt.getIdReceipt());

        List<ImportReceiptDetailResponse> itemResponses = details.stream()
                .map(d -> {
                    ProductVariant v = d.getVariant();
                    String prodName = (v != null && v.getProduct() != null) ? v.getProduct().getName() : "";
                    String sku = v != null ? v.getSku() : "";
                    String capacity = v != null ? v.getCapacity() : "";

                    return ImportReceiptDetailResponse.builder()
                            .idVariant(v != null ? v.getIdVariant() : null)
                            .sku(sku)
                            .capacity(capacity)
                            .productName(prodName)
                            .quantity(d.getQuantity())
                            .importPrice(d.getImportPrice())
                            .subTotal(d.getImportPrice() * d.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        return ImportReceiptResponse.builder()
                .idReceipt(receipt.getIdReceipt())
                .idSupplier(receipt.getSupplier() != null ? receipt.getSupplier().getIdSupplier() : null)
                .supplierName(receipt.getSupplier() != null ? receipt.getSupplier().getName() : null)
                .idWarehouseManager(receipt.getWarehouseManager() != null ? receipt.getWarehouseManager().getIdUser() : null)
                .warehouseManagerName(receipt.getWarehouseManager() != null ? receipt.getWarehouseManager().getFullName() : null)
                .importDate(receipt.getImportDate())
                .totalCost(receipt.getTotalCost())
                .note(receipt.getNote())
                .items(itemResponses)
                .build();
    }

    private InventoryLogResponse mapToInventoryLogResponse(InventoryLog log) {
        ProductVariant v = log.getVariant();
        User u = log.getUser();

        return InventoryLogResponse.builder()
                .idLog(log.getIdLog())
                .idVariant(v != null ? v.getIdVariant() : null)
                .sku(v != null ? v.getSku() : null)
                .productName((v != null && v.getProduct() != null) ? v.getProduct().getName() : null)
                .capacity(v != null ? v.getCapacity() : null)
                .idUser(u != null ? u.getIdUser() : null)
                .userName(u != null ? u.getFullName() : null)
                .transactionType(log.getTransactionType())
                .quantityChange(log.getQuantityChange())
                .referenceId(log.getReferenceId())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
