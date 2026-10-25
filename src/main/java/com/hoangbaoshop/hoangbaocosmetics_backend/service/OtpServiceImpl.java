package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final OtpAttemptHelper otpAttemptHelper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPT_COUNT = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // sinh mã OTP ngẫu nhiên, lưu vào redis/db và gửi email cho người dùng
    @Override
    @Transactional
    public void generateAndSendOtp(String email, OtpTokenType tokenType) {
        String cleanEmail = email.trim().toLowerCase();

        // 1. Kiểm tra Cooldown 60s (Ưu tiên kiểm tra nhanh trên Redis)
        if (redisTemplate != null) {
            String cooldownKey = "otp:cooldown:" + tokenType + ":" + cleanEmail;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
                throw new BadRequestException("Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới!");
            }
        } else {
            otpTokenRepository.findTopByEmailAndTokenTypeOrderByCreatedAtDesc(cleanEmail, tokenType)
                    .ifPresent(latest -> {
                        long elapsedSeconds = Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds();
                        if (elapsedSeconds < RESEND_COOLDOWN_SECONDS) {
                            throw new BadRequestException("Vui lòng đợi " + (RESEND_COOLDOWN_SECONDS - elapsedSeconds) + " giây trước khi yêu cầu mã OTP mới!");
                        }
                    });
        }

        // 2. Vô hiệu hóa các OTP cũ
        otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                .ifPresent(oldToken -> {
                    oldToken.setIsUsed(true);
                    otpTokenRepository.save(oldToken);
                });

        // 3. Sinh mã OTP 6 chữ số ngẫu nhiên an toàn
        String otpCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        // 4. Lưu OTP vào Redis với TTL 5 phút (Tự hủy tự động, giải phóng RAM và I/O)
        if (redisTemplate != null) {
            try {
                String redisKey = "otp:" + tokenType + ":" + cleanEmail;
                String cooldownKey = "otp:cooldown:" + tokenType + ":" + cleanEmail;
                String attemptsKey = "otp:attempts:" + tokenType + ":" + cleanEmail;
                String lockedKey = "otp:locked:" + tokenType + ":" + cleanEmail;

                redisTemplate.opsForValue().set(redisKey, otpCode, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
                redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(RESEND_COOLDOWN_SECONDS));
                redisTemplate.delete(attemptsKey);
                redisTemplate.delete(lockedKey);
                log.info("Đã lưu OTP vào Redis với TTL {} phút cho email: {}", OTP_EXPIRY_MINUTES, cleanEmail);
            } catch (Exception e) {
                log.warn("Lỗi khi ghi OTP vào Redis, tiếp tục lưu MySQL fallback: {}", e.getMessage());
            }
        }

        // Đồng thời lưu bản ghi vào MySQL để đảm bảo tính toàn vẹn kiểm toán (Audit)
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
        findAndValidateOtp(email, otpCode, tokenType, false);
    }

    // hoàn tất việc sử dụng OTP (đánh dấu isUsed = true và xóa khỏi Redis)
    @Override
    @Transactional
    public void consumeOtp(String email, String otpCode, OtpTokenType tokenType) {
        OtpToken token = findAndValidateOtp(email, otpCode, tokenType, true);
        token.setIsUsed(true);
        otpTokenRepository.save(token);

        if (redisTemplate != null) {
            try {
                String cleanEmail = email.trim().toLowerCase();
                redisTemplate.delete("otp:" + tokenType + ":" + cleanEmail);
                redisTemplate.delete("otp:attempts:" + tokenType + ":" + cleanEmail);
                redisTemplate.delete("otp:locked:" + tokenType + ":" + cleanEmail);
            } catch (Exception e) {
                log.warn("Lỗi khi xóa OTP khỏi Redis: {}", e.getMessage());
            }
        }
    }

    // hàm nội bộ tìm kiếm và xác thực tính hợp lệ của mã OTP
    private OtpToken findAndValidateOtp(String email, String otpCode, OtpTokenType tokenType, boolean consume) {
        String cleanEmail = email.trim().toLowerCase();

        // 1. Xác thực bằng Redis nếu có
        if (redisTemplate != null) {
            try {
                String lockedKey = "otp:locked:" + tokenType + ":" + cleanEmail;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(lockedKey))) {
                    throw new BadRequestException("Bạn đã nhập sai mã quá số lần cho phép. Vui lòng yêu cầu mã OTP mới!");
                }

                String redisKey = "otp:" + tokenType + ":" + cleanEmail;
                String attemptsKey = "otp:attempts:" + tokenType + ":" + cleanEmail;
                String storedOtp = redisTemplate.opsForValue().get(redisKey);

                if (storedOtp != null) {
                    Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
                    if (attempts != null && attempts == 1) {
                        redisTemplate.expire(attemptsKey, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
                    }

                    if (attempts != null && attempts >= MAX_ATTEMPT_COUNT && !storedOtp.equals(otpCode.trim())) {
                        redisTemplate.opsForValue().set(lockedKey, "1", Duration.ofMinutes(OTP_EXPIRY_MINUTES));
                        redisTemplate.delete(redisKey);
                        otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                                .ifPresent(t -> otpAttemptHelper.markTokenAsUsed(t.getIdOtp()));
                        throw new BadRequestException("Bạn đã nhập sai mã quá số lần cho phép. Vui lòng yêu cầu mã OTP mới!");
                    }

                    if (!storedOtp.equals(otpCode.trim())) {
                        long remainingAttempts = MAX_ATTEMPT_COUNT - (attempts != null ? attempts : 1);
                        throw new BadRequestException("Mã OTP không chính xác! Bạn còn " + Math.max(0, remainingAttempts) + " lần thử.");
                    }

                    // Khớp mã OTP trong Redis -> đồng bộ tìm token DB để trả về
                    return otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                            .orElseGet(() -> OtpToken.builder()
                                    .email(cleanEmail)
                                    .otpCode(storedOtp)
                                    .tokenType(tokenType)
                                    .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                                    .isUsed(false)
                                    .attemptCount(0)
                                    .build());
                }
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Redis verify OTP lỗi, chuyển sang fallback database: {}", e.getMessage());
            }
        }

        // 2. Fallback sang Database MySQL
        OtpToken token = otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(cleanEmail, tokenType)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy mã OTP hợp lệ hoặc mã đã được sử dụng!"));

        if (token.getAttemptCount() >= MAX_ATTEMPT_COUNT) {
            otpAttemptHelper.markTokenAsUsed(token.getIdOtp());
            throw new BadRequestException("Bạn đã nhập sai mã quá số lần cho phép. Vui lòng yêu cầu mã OTP mới!");
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpAttemptHelper.markTokenAsUsed(token.getIdOtp());
            throw new BadRequestException("Mã OTP đã hết hạn! Vui lòng yêu cầu mã OTP mới.");
        }

        if (!token.getOtpCode().equals(otpCode.trim())) {
            int newAttemptCount = otpAttemptHelper.recordFailedAttempt(token.getIdOtp(), MAX_ATTEMPT_COUNT);
            token.setAttemptCount(newAttemptCount);
            if (newAttemptCount >= MAX_ATTEMPT_COUNT) {
                token.setIsUsed(true);
                throw new BadRequestException("Bạn đã nhập sai mã quá số lần cho phép. Vui lòng yêu cầu mã OTP mới!");
            }
            int remainingAttempts = MAX_ATTEMPT_COUNT - newAttemptCount;
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
