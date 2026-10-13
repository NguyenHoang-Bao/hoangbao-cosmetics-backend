package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.PublicStoreSettingsResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class PublicSettingController {

    private final SystemSettingService systemSettingService;

    // [GET] Lấy các thông số cài đặt công khai cho khách hàng (phí ship, ngưỡng freeship, hotline, tên shop)
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PublicStoreSettingsResponse>> getPublicSettings() {
        PublicStoreSettingsResponse response = systemSettingService.getPublicSettings();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin cấu hình cửa hàng thành công!"));
    }
}
