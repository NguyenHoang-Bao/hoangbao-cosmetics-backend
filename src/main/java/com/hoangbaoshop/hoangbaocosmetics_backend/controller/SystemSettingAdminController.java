package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.setting.UpdateSettingRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting.SystemSettingResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SystemSettingAdminController {

    private final SystemSettingService systemSettingService;

    // [GET] Lấy danh sách toàn bộ cài đặt hệ thống cho Admin
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAllSettings() {
        List<SystemSettingResponse> response = systemSettingService.getAllSettingsAdmin();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách cài đặt hệ thống thành công!"));
    }

    // [PUT] Cập nhật giá trị cài đặt hệ thống theo Key
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        SystemSettingResponse response = systemSettingService.updateSettingAdmin(key, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật cài đặt hệ thống thành công!"));
    }
}
