package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ChangePasswordRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateProfileRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.UserProfileResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Các API tra cứu và cập nhật hồ sơ khách hàng")
public class UserController {

    private final UserService userService;

    // [GET] /api/users/profile - Lấy thông tin cá nhân của người dùng đang đăng nhập
    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin cá nhân của người dùng hiện tại")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserProfileResponse response = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [PUT] /api/users/profile - Cập nhật thông tin cá nhân
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân (họ tên, số điện thoại, email, avatar)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật hồ sơ thành công!"));
    }

    // [PUT] /api/users/change-password - Đổi mật khẩu tài khoản
    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu tài khoản người dùng")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công!"));
    }
}
