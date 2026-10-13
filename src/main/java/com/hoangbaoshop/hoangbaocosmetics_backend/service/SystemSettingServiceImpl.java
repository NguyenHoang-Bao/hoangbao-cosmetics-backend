package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.setting.UpdateSettingRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.PublicStoreSettingsResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.SystemSettingResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.SystemSetting;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.SettingDataType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    // Cache in-memory hiệu năng cao O(1), tự động hot-reload khi Admin cập nhật cấu hình
    private final Map<String, String> settingCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        refreshCache();
    }

    public synchronized void refreshCache() {
        try {
            List<SystemSetting> settings = systemSettingRepository.findAll();
            settingCache.clear();
            for (SystemSetting s : settings) {
                if (s.getSettingKey() != null && s.getSettingValue() != null) {
                    settingCache.put(s.getSettingKey(), s.getSettingValue());
                }
            }
            log.info("[SystemSettingService] Đã nạp thành công {} cấu hình hệ thống vào Cache.", settingCache.size());
        } catch (Exception e) {
            log.warn("[SystemSettingService] Chưa thể nạp cache cấu hình khi khởi động (DB có thể chưa sẵn sàng): {}", e.getMessage());
        }
    }

    @Override
    public String getValue(String key, String defaultValue) {
        String value = settingCache.get(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return defaultValue;
    }

    @Override
    public BigDecimal getBigDecimalValue(String key, BigDecimal defaultValue) {
        String value = getValue(key, null);
        if (value != null) {
            try {
                return new BigDecimal(value.trim());
            } catch (NumberFormatException e) {
                log.warn("[SystemSettingService] Lỗi parse BigDecimal cho key '{}': {}", key, value);
            }
        }
        return defaultValue;
    }

    @Override
    public int getIntValue(String key, int defaultValue) {
        String value = getValue(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                log.warn("[SystemSettingService] Lỗi parse int cho key '{}': {}", key, value);
            }
        }
        return defaultValue;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSettingsAdmin() {
        return systemSettingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SystemSettingResponse updateSettingAdmin(String key, UpdateSettingRequest request) {
        SystemSetting setting = systemSettingRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cấu hình với mã: " + key));

        String newValue = request.getSettingValue().trim();
        validateDataType(setting.getDataType(), newValue, key);

        setting.setSettingValue(newValue);
        SystemSetting savedSetting = systemSettingRepository.save(setting);

        // Hot-reload Cache ngay lập tức mà không cần restart server
        settingCache.put(key, savedSetting.getSettingValue());
        log.info("[SystemSettingService] Admin đã cập nhật cấu hình '{}' = '{}' (Hot-reloaded)", key, newValue);

        return mapToResponse(savedSetting);
    }

    @Override
    public PublicStoreSettingsResponse getPublicSettings() {
        BigDecimal shippingFee = getBigDecimalValue("DEFAULT_SHIPPING_FEE", BigDecimal.valueOf(30000));
        BigDecimal freeShippingThreshold = getBigDecimalValue("FREE_SHIPPING_THRESHOLD", BigDecimal.valueOf(500000));
        String storeName = getValue("STORE_NAME", "Hoàng Bảo Cosmetics");
        String hotline = getValue("STORE_HOTLINE", "0912345678");
        String email = getValue("STORE_EMAIL", "hbao4142@gmail.com");

        return PublicStoreSettingsResponse.builder()
                .defaultShippingFee(shippingFee)
                .freeShippingThreshold(freeShippingThreshold)
                .storeName(storeName)
                .storeHotline(hotline)
                .storeEmail(email)
                .build();
    }

    private void validateDataType(SettingDataType dataType, String value, String key) {
        if (dataType == SettingDataType.NUMBER) {
            try {
                new BigDecimal(value);
            } catch (Exception e) {
                throw new BadRequestException("Cấu hình '" + key + "' yêu cầu giá trị kiểu số hợp lệ!");
            }
        } else if (dataType == SettingDataType.BOOLEAN) {
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                throw new BadRequestException("Cấu hình '" + key + "' yêu cầu giá trị true hoặc false!");
            }
        }
    }

    private SystemSettingResponse mapToResponse(SystemSetting setting) {
        return SystemSettingResponse.builder()
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .dataType(setting.getDataType())
                .description(setting.getDescription())
                .isPublic(setting.getIsPublic())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
