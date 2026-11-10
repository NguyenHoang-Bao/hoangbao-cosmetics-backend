package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.CreateStaffRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ResetPasswordAdminRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateUserRoleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.AdminUserResponse;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    // Danh sách tài khoản người dùng/nhân sự có tìm kiếm, lọc theo vai trò và phân trang
    PageResponse<AdminUserResponse> getAllUsers(String keyword, String roleName, Boolean enabled, Pageable pageable);

    // Xem chi tiết tài khoản theo ID
    AdminUserResponse getUserById(Integer idUser);

    // Tạo mới tài khoản nhân viên (POS, Kho, CSKH Online, Admin)
    AdminUserResponse createStaff(CreateStaffRequest request);

    // Khóa hoặc mở khóa tài khoản người dùng
    AdminUserResponse toggleUserStatus(Integer idUser, String currentAdminUsername);

    // Cập nhật vai trò / phân quyền nhân sự
    AdminUserResponse updateUserRole(Integer idUser, UpdateUserRoleRequest request, String currentAdminUsername);

    // Admin đặt lại mật khẩu mới cho nhân sự khi quên mật khẩu
    void resetPassword(Integer idUser, ResetPasswordAdminRequest request);
}
