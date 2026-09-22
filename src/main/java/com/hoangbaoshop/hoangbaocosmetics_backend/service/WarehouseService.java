package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.CreateImportReceiptRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.InventoryLogResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface WarehouseService {

    ImportReceiptResponse createImportReceipt(String username, CreateImportReceiptRequest request);

    ImportReceiptResponse getImportReceiptById(Integer id);

    PageResponse<ImportReceiptResponse> getAllImportReceipts(
            Integer supplierId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    PageResponse<InventoryLogResponse> getInventoryLogs(
            Integer variantId,
            TransactionType transactionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
