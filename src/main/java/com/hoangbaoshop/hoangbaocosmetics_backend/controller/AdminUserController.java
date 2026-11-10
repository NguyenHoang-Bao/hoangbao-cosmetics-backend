package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.CreateStaffRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ResetPasswordAdminRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateUserRoleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.AdminUserResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User & Staff Management", description = "Các API tra cứu, thêm nhân sự, phân quyền và đặt lại mật khẩu")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // [GET] /api/admin/users - Danh sách người dùng & nhân sự phân trang, tìm kiếm, lọc vai trò
    @GetMapping
    @Operation(summary = "Lấy danh sách tài khoản người dùng và nhân sự (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "idUser") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<AdminUserResponse> response = adminUserService.getAllUsers(keyword, role, enabled, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [GET] /api/admin/users/{id} - Xem chi tiết tài khoản
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết tài khoản theo ID (Admin)")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getUserById(id)));
    }

    // [POST] /api/admin/users - Tạo tài khoản nhân viên mới (POS, Kho, CSKH, Admin)
    @PostMapping
    @Operation(summary = "Tạo mới tài khoản nhân viên (Admin)")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request
    ) {
        AdminUserResponse response = adminUserService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo tài khoản nhân viên thành công!"));
    }

    // [PATCH] /api/admin/users/{id}/status - Khóa / Mở khóa tài khoản
    @PatchMapping("/{id}/status")
    @Operation(summary = "Khóa hoặc Mở khóa tài khoản (Admin)")
    public ResponseEntity<ApiResponse<AdminUserResponse>> toggleUserStatus(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentAdmin = userDetails != null ? userDetails.getUsername() : null;
        AdminUserResponse response = adminUserService.toggleUserStatus(id, currentAdmin);
        String msg = Boolean.TRUE.equals(response.getEnabled()) ? "Mở khóa tài khoản thành công!" : "Đã khóa tài khoản thành công!";
        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }

    // [PATCH] /api/admin/users/{id}/role - Cập nhật vai trò / phân quyền
    @PatchMapping("/{id}/role")
    @Operation(summary = "Thay đổi vai trò / phân quyền người dùng (Admin)")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentAdmin = userDetails != null ? userDetails.getUsername() : null;
        AdminUserResponse response = adminUserService.updateUserRole(id, request, currentAdmin);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật vai trò thành công!"));
    }

    // [POST] /api/admin/users/{id}/reset-password - Đặt lại mật khẩu nhân viên
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Admin đặt lại mật khẩu cho tài khoản nhân viên")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordAdminRequest request
    ) {
        adminUserService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu thành công!"));
    }
}
