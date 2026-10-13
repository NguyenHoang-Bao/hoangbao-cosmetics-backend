package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.setting.UpdateSettingRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.PublicStoreSettingsResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.SystemSettingResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.SystemSetting;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.SettingDataType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceImplTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private SystemSettingServiceImpl systemSettingService;

    @BeforeEach
    void setUp() {
        SystemSetting shippingFee = SystemSetting.builder()
                .settingKey("DEFAULT_SHIPPING_FEE")
                .settingValue("35000")
                .dataType(SettingDataType.NUMBER)
                .isPublic(true)
                .build();

        SystemSetting storeName = SystemSetting.builder()
                .settingKey("STORE_NAME")
                .settingValue("Hoàng Bảo Cosmetics Test")
                .dataType(SettingDataType.STRING)
                .isPublic(true)
                .build();

        when(systemSettingRepository.findAll()).thenReturn(List.of(shippingFee, storeName));
        systemSettingService.refreshCache();
    }

    @Test
    void testGetBigDecimalValue_ReturnsCachedValue() {
        BigDecimal fee = systemSettingService.getBigDecimalValue("DEFAULT_SHIPPING_FEE", BigDecimal.valueOf(30000));
        assertEquals(new BigDecimal("35000"), fee);
    }

    @Test
    void testGetValue_WhenKeyMissing_ReturnsDefaultValue() {
        String missing = systemSettingService.getValue("UNKNOWN_KEY", "DefaultValue");
        assertEquals("DefaultValue", missing);
    }

    @Test
    void testUpdateSettingAdmin_HotReloadsCache() {
        SystemSetting setting = SystemSetting.builder()
                .settingKey("DEFAULT_SHIPPING_FEE")
                .settingValue("35000")
                .dataType(SettingDataType.NUMBER)
                .isPublic(true)
                .build();

        when(systemSettingRepository.findById("DEFAULT_SHIPPING_FEE")).thenReturn(Optional.of(setting));
        when(systemSettingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSettingRequest request = new UpdateSettingRequest("25000");
        SystemSettingResponse response = systemSettingService.updateSettingAdmin("DEFAULT_SHIPPING_FEE", request);

        assertNotNull(response);
        assertEquals("25000", response.getSettingValue());

        // Verify cache is immediately updated
        BigDecimal updatedFee = systemSettingService.getBigDecimalValue("DEFAULT_SHIPPING_FEE", BigDecimal.ZERO);
        assertEquals(new BigDecimal("25000"), updatedFee);
    }

    @Test
    void testUpdateSettingAdmin_InvalidNumber_ThrowsBadRequestException() {
        SystemSetting setting = SystemSetting.builder()
                .settingKey("DEFAULT_SHIPPING_FEE")
                .settingValue("35000")
                .dataType(SettingDataType.NUMBER)
                .isPublic(true)
                .build();

        when(systemSettingRepository.findById("DEFAULT_SHIPPING_FEE")).thenReturn(Optional.of(setting));

        UpdateSettingRequest request = new UpdateSettingRequest("NOT_A_NUMBER");
        assertThrows(BadRequestException.class, () -> systemSettingService.updateSettingAdmin("DEFAULT_SHIPPING_FEE", request));
    }

    @Test
    void testGetPublicSettings_ReturnsPopulatedDto() {
        PublicStoreSettingsResponse response = systemSettingService.getPublicSettings();
        assertNotNull(response);
        assertEquals(new BigDecimal("35000"), response.getDefaultShippingFee());
        assertEquals("Hoàng Bảo Cosmetics Test", response.getStoreName());
    }
}
