package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogResponse {

    private Integer idLog;
    private Integer idVariant;
    private String sku;
    private String productName;
    private String capacity;
    private Integer idUser;
    private String userName;
    private TransactionType transactionType;
    private Integer quantityChange;
    private Integer referenceId;
    private LocalDateTime createdAt;
}
