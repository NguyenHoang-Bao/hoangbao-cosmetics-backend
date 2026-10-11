package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.CreateImportReceiptRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.InventoryLogResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface WarehouseService {

    // tạo phiếu nhập kho (cộng số lượng tồn kho + ghi sổ cái inventory log)
    ImportReceiptResponse createImportReceipt(String username, CreateImportReceiptRequest request);

    // lấy chi tiết phiếu nhập kho theo id
    ImportReceiptResponse getImportReceiptById(Integer id);

    // lấy danh sách phiếu nhập kho (lọc theo ncc, ngày & phân trang)
    PageResponse<ImportReceiptResponse> getAllImportReceipts(
            Integer supplierId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // xem sổ cái lịch sử biến động kho (nhập hàng, bán hàng, hoàn kho...)
    PageResponse<InventoryLogResponse> getInventoryLogs(
            Integer variantId,
            TransactionType transactionType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
