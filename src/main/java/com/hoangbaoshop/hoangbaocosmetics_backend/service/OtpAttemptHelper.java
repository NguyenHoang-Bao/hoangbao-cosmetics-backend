package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpAttemptHelper {

    private final OtpTokenRepository otpTokenRepository;

    /**
     * Tăng số lần thử sai trong transaction độc lập (REQUIRES_NEW).
     * Đảm bảo số lần nhập sai và trạng thái khóa được commit ngay lập tức vào MySQL,
     * không bị rollback khi hàm gọi ném BadRequestException.
     *
     * @param idOtp ID của OtpToken
     * @param maxAttempts Số lần thử tối đa cho phép
     * @return Số lần đã thử sau khi tăng
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(Long idOtp, int maxAttempts) {
        return otpTokenRepository.findById(idOtp).map(token -> {
            int newCount = token.getAttemptCount() + 1;
            token.setAttemptCount(newCount);
            if (newCount >= maxAttempts) {
                token.setIsUsed(true);
            }
            otpTokenRepository.saveAndFlush(token);
            log.info("Cập nhật số lần thử OTP độc lập [REQUIRES_NEW]: idOtp={}, attempts={}/{}", idOtp, newCount, maxAttempts);
            return newCount;
        }).orElse(0);
    }

    /**
     * Đánh dấu token đã sử dụng / vô hiệu hóa trong transaction độc lập (REQUIRES_NEW).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTokenAsUsed(Long idOtp) {
        otpTokenRepository.findById(idOtp).ifPresent(token -> {
            token.setIsUsed(true);
            otpTokenRepository.saveAndFlush(token);
            log.info("Vô hiệu hóa OTP token độc lập [REQUIRES_NEW]: idOtp={}", idOtp);
        });
    }
}
