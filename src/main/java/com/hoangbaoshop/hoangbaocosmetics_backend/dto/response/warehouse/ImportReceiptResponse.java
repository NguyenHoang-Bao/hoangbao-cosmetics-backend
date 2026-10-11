package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptResponse {

    private Integer idReceipt;
    private Integer idSupplier;
    private String supplierName;
    private Integer idWarehouseManager;
    private String warehouseManagerName;
    private LocalDateTime importDate;
    private BigDecimal totalCost;
    private String note;
    private List<ImportReceiptDetailResponse> items;
}
