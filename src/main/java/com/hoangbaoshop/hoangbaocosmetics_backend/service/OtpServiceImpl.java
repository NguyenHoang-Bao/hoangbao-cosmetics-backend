package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPT_COUNT = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // sinh mã OTP ngẫu nhiên, lưu vào db và gửi email cho người dùng
    @Override
    @Transactional
    public void generateAndSendOtp(String email, OtpTokenType tokenType) {
        String cleanEmail = email.trim().toLowerCase();

        // 1. Kiểm tra giới hạn thời gian gửi lại (Cooldown 60s)
        otpTokenRepository.findTopByEmailAndTokenTypeOrderByCreatedAtDesc(cleanEmail, tokenType)
                .ifPresent(latest -> {
                    long elapsedSeconds = Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    if (elapsedSeconds < RESEND_COOLDOWN_SECONDS) {
                        throw new BadRequestException("Vui lòng đợi " + (RESEND_COOLDOWN_SECONDS - elapsedSeconds) + " giây trước khi yêu cầu mã OTP mới!");
                    }
                });

        // 2. Vô hiệu hóa các OTP cũ chưa sử dụng của email này
        otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                .ifPresent(oldToken -> {
                    oldToken.setIsUsed(true);
                    otpTokenRepository.save(oldToken);
                });

        // 3. Sinh mã OTP 6 chữ số ngẫu nhiên an toàn
        String otpCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        // 4. Lưu OTP vào cơ sở dữ liệu với thời hạn 5 phút
        OtpToken otpToken = OtpToken.builder()
                .email(cleanEmail)
                .otpCode(otpCode)
                .tokenType(tokenType)
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .attemptCount(0)
                .build();

        otpTokenRepository.save(otpToken);

        // 5. Gửi email bất đồng bộ sau khi transaction commit thành công
        executeAfterCommit(() -> emailService.sendOtpEmail(cleanEmail, otpCode, tokenType));
        log.info("Đã tạo và kích hoạt gửi OTP cho email: {} [Type: {}]", cleanEmail, tokenType);
    }

    // kiểm tra tính hợp lệ của mã OTP
    @Override
    @Transactional
    public void verifyOtp(String email, String otpCode, OtpTokenType tokenType) {
        findAndValidateOtp(email, otpCode, tokenType);
    }

    // hoàn tất việc sử dụng OTP (đánh dấu isUsed = true)
    @Override
    @Transactional
    public void consumeOtp(String email, String otpCode, OtpTokenType tokenType) {
        OtpToken token = findAndValidateOtp(email, otpCode, tokenType);
        token.setIsUsed(true);
        otpTokenRepository.save(token);
    }

    // hàm nội bộ tìm kiếm và xác thực tính hợp lệ của mã OTP
    private OtpToken findAndValidateOtp(String email, String otpCode, OtpTokenType tokenType) {
        String cleanEmail = email.trim().toLowerCase();

        OtpToken token = otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy mã OTP hợp lệ hoặc mã đã được sử dụng!"));

        // Kiểm tra số lần nhập sai tối đa
        if (token.getAttemptCount() >= MAX_ATTEMPT_COUNT) {
            token.setIsUsed(true);
            otpTokenRepository.save(token);
            throw new BadRequestException("Bạn đã nhập sai mã quá số lần cho phép. Vui lòng yêu cầu mã OTP mới!");
        }

        // Kiểm tra thời hạn hiệu lực
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            token.setIsUsed(true);
            otpTokenRepository.save(token);
            throw new BadRequestException("Mã OTP đã hết hạn! Vui lòng yêu cầu mã OTP mới.");
        }

        // Kiểm tra độ khớp của mã OTP
        if (!token.getOtpCode().equals(otpCode.trim())) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            otpTokenRepository.save(token);
            int remainingAttempts = MAX_ATTEMPT_COUNT - token.getAttemptCount();
            throw new BadRequestException("Mã OTP không chính xác! Bạn còn " + remainingAttempts + " lần thử.");
        }

        return token;
    }

    // thực thi tác vụ sau khi database transaction đã commit thành công
    private void executeAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
