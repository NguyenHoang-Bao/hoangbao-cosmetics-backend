package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // lấy mã OTP mới nhất chưa sử dụng theo email và loại OTP
    Optional<OtpToken> findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(String email, OtpTokenType tokenType);

    // lấy mã OTP mới nhất theo email và loại OTP để kiểm tra cooldown gửi lại
    Optional<OtpToken> findTopByEmailAndTokenTypeOrderByCreatedAtDesc(String email, OtpTokenType tokenType);

    // lấy token xác thực theo mã code, loại token và chưa được sử dụng
    Optional<OtpToken> findTopByOtpCodeAndTokenTypeAndIsUsedFalse(String otpCode, OtpTokenType tokenType);
}
