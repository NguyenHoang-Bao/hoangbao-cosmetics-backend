package com.hoangbaoshop.hoangbaocosmetics_backend.scheduler;

import com.hoangbaoshop.hoangbaocosmetics_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tác vụ định kỳ quét và tự động hủy các đơn hàng thanh toán chuyển khoản quá hạn 30 phút,
 * giải phóng và hoàn lại số lượng tồn kho (chống giữ kho ảo / DoS kho).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredOrderScheduler {

    private final OrderService orderService;

    /**
     * Chạy định kỳ mỗi 5 phút (300,000 ms) sau khi tác vụ trước hoàn thành.
     */
    @Scheduled(fixedDelay = 300_000)
    public void run() {
        try {
            int cancelledCount = orderService.cancelExpiredBankTransferOrders();
            if (cancelledCount > 0) {
                log.info("[ExpiredOrderScheduler] Đã tự động hủy {} đơn hàng chuyển khoản quá hạn 30 phút và hoàn lại kho.", cancelledCount);
            }
        } catch (Exception e) {
            log.error("[ExpiredOrderScheduler] Lỗi khi quét hủy đơn hàng quá hạn: {}", e.getMessage(), e);
        }
    }
}
