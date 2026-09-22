package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateImportReceiptRequest {

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Integer idSupplier;

    private String note;

    @NotEmpty(message = "Phiếu nhập phải có ít nhất một sản phẩm")
    @Valid
    private List<ImportReceiptItemRequest> items;
}
