package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.CreateImportReceiptRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.ImportReceiptResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.InventoryLogResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // 1. Lập phiếu nhập kho mới
    @PostMapping("/import-receipts")
    public ResponseEntity<ApiResponse<ImportReceiptResponse>> createImportReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateImportReceiptRequest request
    ) {
        ImportReceiptResponse response = warehouseService.createImportReceipt(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lập phiếu nhập kho thành công!"));
    }

    // 2. Lấy chi tiết phiếu nhập kho
    @GetMapping("/import-receipts/{id}")
    public ResponseEntity<ApiResponse<ImportReceiptResponse>> getImportReceiptById(@PathVariable Integer id) {
        ImportReceiptResponse response = warehouseService.getImportReceiptById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Lấy danh sách phiếu nhập kho phân trang & lọc theo nhà cung cấp, ngày
    @GetMapping("/import-receipts")
    public ResponseEntity<ApiResponse<PageResponse<ImportReceiptResponse>>> getAllImportReceipts(
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "importDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ImportReceiptResponse> response = warehouseService.getAllImportReceipts(supplierId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. Tra cứu Sổ cái biến động kho (Audit log)
    @GetMapping("/inventory-logs")
    public ResponseEntity<ApiResponse<PageResponse<InventoryLogResponse>>> getInventoryLogs(
            @RequestParam(required = false) Integer variantId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<InventoryLogResponse> response = warehouseService.getInventoryLogs(variantId, transactionType, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
