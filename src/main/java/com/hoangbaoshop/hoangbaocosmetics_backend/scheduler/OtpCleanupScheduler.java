package com.hoangbaoshop.hoangbaocosmetics_backend.scheduler;

import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Tác vụ định kỳ (Cron Job) dọn dẹp các mã OTP rác đã hết hạn hoặc đã sử dụng.
 * Chạy ngầm trong hệ thống giúp tối ưu dung lượng Database và hiệu năng truy vấn.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final OtpTokenRepository otpTokenRepository;

    /**
     * Tự động dọn dẹp các mã OTP rác đã hết hạn hoặc đã được sử dụng.
     * Chạy định kỳ vào 02:00:00 sáng mỗi ngày (giờ thấp điểm ít người dùng).
     * Cron expression: Giây (0) Phút (0) Giờ (2) Ngày-trong-tháng (*) Tháng (*) Thứ (?)
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void cleanupExpiredOtpTokens() {
        log.info("[CronJob] Bắt đầu quét và dọn dẹp mã OTP hết hạn / đã sử dụng...");
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = otpTokenRepository.deleteExpiredOrUsedTokens(now);
        if (deletedCount > 0) {
            log.info("[CronJob] Đã dọn dẹp thành công {} bản ghi OTP rác khỏi Database.", deletedCount);
        } else {
            log.info("[CronJob] Không có mã OTP rác nào cần dọn dẹp.");
        }
    }
}
