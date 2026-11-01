package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleRequest {

    @NotBlank(message = "Tiêu đề Flash Sale không được để trống")
    private String title;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    @NotEmpty(message = "Danh sách sản phẩm Flash Sale không được để trống")
    @Valid
    private List<FlashSaleItemRequest> items;
}
