package com.hoangbaoshop.hoangbaocosmetics_backend.client;

import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GoogleClientTest {

    @InjectMocks
    private GoogleClient googleClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleClient, "googleClientId", "test-client-id.apps.googleusercontent.com");
    }

    @Test
    void verifyAndGetProfile_WhenTokenIsEmpty_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                googleClient.verifyAndGetProfile("   ")
        );
        assertTrue(ex.getMessage().contains("không được để trống"));
    }

    @Test
    void verifyAndGetProfile_WhenInvalidTokenProvided_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                googleClient.verifyAndGetProfile("invalid-dummy-token")
        );
        assertTrue(ex.getMessage().contains("không hợp lệ hoặc đã hết hạn"));
    }
}
