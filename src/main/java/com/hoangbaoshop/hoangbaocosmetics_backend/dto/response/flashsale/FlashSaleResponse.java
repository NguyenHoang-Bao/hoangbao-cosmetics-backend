package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.FlashSaleStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleResponse {

    private Integer idFlashSale;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private FlashSaleStatus status;

    // Số giây còn lại đến khi kết thúc (dành cho countdown timer client đồng bộ)
    private Long remainingSeconds;

    // Danh sách sản phẩm trong đợt sale
    private List<FlashSaleItemResponse> items;
}
