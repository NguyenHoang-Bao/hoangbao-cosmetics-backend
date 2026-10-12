package com.hoangbaoshop.hoangbaocosmetics_backend.scheduler;

import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpCleanupSchedulerTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @InjectMocks
    private OtpCleanupScheduler otpCleanupScheduler;

    @Test
    void testCleanupExpiredOtpTokens_CallsRepositoryDelete() {
        when(otpTokenRepository.deleteExpiredOrUsedTokens(any(LocalDateTime.class))).thenReturn(5);

        otpCleanupScheduler.cleanupExpiredOtpTokens();

        verify(otpTokenRepository, times(1)).deleteExpiredOrUsedTokens(any(LocalDateTime.class));
    }
}
