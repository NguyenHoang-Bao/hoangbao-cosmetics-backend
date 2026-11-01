package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale.FlashSaleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
@Tag(name = "Flash Sale Controller", description = "APIs quản lý và hiển thị chiến dịch Flash Sale")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/active")
    @Operation(summary = "Lấy chiến dịch Flash Sale đang diễn ra (Public cho trang chủ)")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> getActiveFlashSale() {
        return ResponseEntity.ok(ApiResponse.success(flashSaleService.getActiveFlashSale()));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả các đợt Flash Sale")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getAllFlashSales() {
        return ResponseEntity.ok(ApiResponse.success(flashSaleService.getAllFlashSales()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đợt Flash Sale theo ID")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> getFlashSaleById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(flashSaleService.getFlashSaleById(id)));
    }

    @PostMapping
    @Operation(summary = "Tạo chiến dịch Flash Sale mới (Admin)")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> createFlashSale(@Valid @RequestBody FlashSaleRequest request) {
        FlashSaleResponse created = flashSaleService.createFlashSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đợt Flash Sale theo ID (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteFlashSale(@PathVariable Integer id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
