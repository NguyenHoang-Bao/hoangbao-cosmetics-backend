package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.setting.UpdateSettingRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.PublicStoreSettingsResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.SystemSettingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SystemSettingService {

    // Lấy giá trị cài đặt dạng chuỗi (đọc từ cache in-memory O(1))
    String getValue(String key, String defaultValue);

    // Lấy giá trị cài đặt dạng BigDecimal (tiền tệ, phí ship)
    BigDecimal getBigDecimalValue(String key, BigDecimal defaultValue);

    // Lấy giá trị cài đặt dạng số nguyên int (thời gian hết hạn, giới hạn số lượng)
    int getIntValue(String key, int defaultValue);

    // Lấy danh sách toàn bộ cấu hình cho Admin
    List<SystemSettingResponse> getAllSettingsAdmin();

    // Cập nhật giá trị cấu hình từ Admin (cập nhật DB + Hot-reload Cache O(1))
    SystemSettingResponse updateSettingAdmin(String key, UpdateSettingRequest request);

    // Lấy thông tin cấu hình công khai cho khách xem (phí ship, hotline, tên shop)
    PublicStoreSettingsResponse getPublicSettings();
}
