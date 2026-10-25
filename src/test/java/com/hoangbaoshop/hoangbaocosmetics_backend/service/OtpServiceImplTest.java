package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpAttemptHelper otpAttemptHelper;

    @InjectMocks
    private OtpServiceImpl otpService;

    @Test
    void testGenerateAndSendOtp_Success() {
        String email = "customer@gmail.com";
        when(otpTokenRepository.findTopByEmailAndTokenTypeOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.empty());
        when(otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.empty());

        otpService.generateAndSendOtp(email, OtpTokenType.FORGOT_PASSWORD);

        verify(otpTokenRepository).save(any(OtpToken.class));
        verify(emailService).sendOtpEmail(eq(email), anyString(), eq(OtpTokenType.FORGOT_PASSWORD));
    }

    @Test
    void testGenerateAndSendOtp_CooldownError() {
        String email = "customer@gmail.com";
        OtpToken recentToken = OtpToken.builder()
                .email(email)
                .tokenType(OtpTokenType.FORGOT_PASSWORD)
                .createdAt(LocalDateTime.now().minusSeconds(20))
                .build();

        when(otpTokenRepository.findTopByEmailAndTokenTypeOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.of(recentToken));

        assertThrows(BadRequestException.class, () ->
                otpService.generateAndSendOtp(email, OtpTokenType.FORGOT_PASSWORD)
        );

        verify(emailService, never()).sendOtpEmail(anyString(), anyString(), any());
    }

    @Test
    void testVerifyOtp_Success() {
        String email = "customer@gmail.com";
        String otp = "123456";
        OtpToken token = OtpToken.builder()
                .idOtp(1L)
                .email(email)
                .otpCode(otp)
                .tokenType(OtpTokenType.FORGOT_PASSWORD)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attemptCount(0)
                .isUsed(false)
                .build();

        when(otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> otpService.verifyOtp(email, otp, OtpTokenType.FORGOT_PASSWORD));
    }

    @Test
    void testVerifyOtp_WrongCode_IncrementsAttemptsViaHelper() {
        String email = "customer@gmail.com";
        OtpToken token = OtpToken.builder()
                .idOtp(1L)
                .email(email)
                .otpCode("123456")
                .tokenType(OtpTokenType.FORGOT_PASSWORD)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attemptCount(0)
                .isUsed(false)
                .build();

        when(otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.of(token));
        when(otpAttemptHelper.recordFailedAttempt(eq(1L), anyInt())).thenReturn(1);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                otpService.verifyOtp(email, "999999", OtpTokenType.FORGOT_PASSWORD)
        );

        assertTrue(ex.getMessage().contains("Mã OTP không chính xác"));
        assertEquals(1, token.getAttemptCount());
        verify(otpAttemptHelper, times(1)).recordFailedAttempt(eq(1L), eq(5));
    }

    @Test
    void testVerifyOtp_ExceedsMaxAttempts_ThrowsLockoutException() {
        String email = "customer@gmail.com";
        OtpToken token = OtpToken.builder()
                .idOtp(1L)
                .email(email)
                .otpCode("123456")
                .tokenType(OtpTokenType.FORGOT_PASSWORD)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .attemptCount(4)
                .isUsed(false)
                .build();

        when(otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.of(token));
        when(otpAttemptHelper.recordFailedAttempt(eq(1L), anyInt())).thenReturn(5);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                otpService.verifyOtp(email, "999999", OtpTokenType.FORGOT_PASSWORD)
        );

        assertTrue(ex.getMessage().contains("Bạn đã nhập sai mã quá số lần cho phép"));
        assertTrue(token.getIsUsed());
        verify(otpAttemptHelper, times(1)).recordFailedAttempt(eq(1L), eq(5));
    }

    @Test
    void testVerifyOtp_Expired_MarksAsUsedViaHelper() {
        String email = "customer@gmail.com";
        OtpToken token = OtpToken.builder()
                .idOtp(1L)
                .email(email)
                .otpCode("123456")
                .tokenType(OtpTokenType.FORGOT_PASSWORD)
                .expiryTime(LocalDateTime.now().minusMinutes(1))
                .attemptCount(0)
                .isUsed(false)
                .build();

        when(otpTokenRepository.findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(email, OtpTokenType.FORGOT_PASSWORD))
                .thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () ->
                otpService.verifyOtp(email, "123456", OtpTokenType.FORGOT_PASSWORD)
        );
        verify(otpAttemptHelper, times(1)).markTokenAsUsed(eq(1L));
    }
}
