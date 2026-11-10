package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.CreateStaffRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ResetPasswordAdminRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateUserRoleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.AdminUserResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllUsers(String keyword, String roleName, Boolean enabled, Pageable pageable) {
        Page<User> rawPage = userRepository.findAll(pageable);
        List<User> filtered = rawPage.getContent().stream()
                .filter(u -> {
                    if (keyword == null || keyword.trim().isBlank()) return true;
                    String kw = keyword.trim().toLowerCase();
                    boolean matchName = u.getFullName() != null && u.getFullName().toLowerCase().contains(kw);
                    boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(kw);
                    boolean matchPhone = u.getPhoneNumber() != null && u.getPhoneNumber().contains(kw);
                    boolean matchUser = u.getUsername() != null && u.getUsername().toLowerCase().contains(kw);
                    return matchName || matchEmail || matchPhone || matchUser;
                })
                .filter(u -> {
                    if (roleName == null || roleName.trim().isBlank() || roleName.equalsIgnoreCase("all")) return true;
                    String targetRole = normalizeRoleName(roleName.trim());
                    return u.getRoles().stream().anyMatch(r -> r.getNameRole().equalsIgnoreCase(targetRole));
                })
                .filter(u -> {
                    if (enabled == null) return true;
                    return Objects.equals(u.getEnabled(), enabled);
                })
                .collect(Collectors.toList());

        List<AdminUserResponse> content = filtered.stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        Page<AdminUserResponse> resultPage = new PageImpl<>(content, pageable, rawPage.getTotalElements());

        return PageResponse.<AdminUserResponse>builder()
                .content(resultPage.getContent())
                .pageNo(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .last(resultPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Integer idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + idUser));
        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createStaff(CreateStaffRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Tên đăng nhập '" + username + "' đã được sử dụng");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email '" + email + "' đã tồn tại trong hệ thống");
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isBlank()) {
            String phone = request.getPhoneNumber().trim();
            if (userRepository.existsByPhoneNumber(phone)) {
                throw new BadRequestException("Số điện thoại '" + phone + "' đã tồn tại");
            }
        }

        String rawRole = normalizeRoleName(request.getRoleName());
        Role role = roleRepository.findByNameRole(rawRole)
                .orElseThrow(() -> new BadRequestException("Vai trò '" + request.getRoleName() + "' không tồn tại"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isBlank())
                ? request.getPassword().trim()
                : "HoangBao@123";

        User newStaff = User.builder()
                .username(username)
                .fullName(request.getFullName().trim())
                .email(email)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .emailVerified(true)
                .authProvider("LOCAL")
                .roles(new HashSet<>(Collections.singletonList(role)))
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(newStaff);
        log.info("Admin đã tạo tài khoản nhân viên mới: {} với vai trò: {}", saved.getUsername(), role.getNameRole());
        return mapToAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse toggleUserStatus(Integer idUser, String currentAdminUsername) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + idUser));

        if (user.getIdUser().equals(1) || (currentAdminUsername != null && currentAdminUsername.equalsIgnoreCase(user.getUsername()))) {
            throw new BadRequestException("Không thể khóa tài khoản của chính bạn hoặc tài khoản Quản trị viên gốc!");
        }

        boolean currentStatus = user.getEnabled() != null ? user.getEnabled() : true;
        user.setEnabled(!currentStatus);
        User updated = userRepository.save(user);

        log.info("Admin [{}] đã thay đổi trạng thái tài khoản [{}] thành: {}", currentAdminUsername, user.getUsername(), updated.getEnabled());
        return mapToAdminUserResponse(updated);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Integer idUser, UpdateUserRoleRequest request, String currentAdminUsername) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + idUser));

        if (user.getIdUser().equals(1) || (currentAdminUsername != null && currentAdminUsername.equalsIgnoreCase(user.getUsername()))) {
            throw new BadRequestException("Không thể thay đổi quyền tài khoản của chính bạn hoặc tài khoản Quản trị viên gốc!");
        }

        String targetRole = normalizeRoleName(request.getRoleName());
        Role role = roleRepository.findByNameRole(targetRole)
                .orElseThrow(() -> new BadRequestException("Vai trò '" + request.getRoleName() + "' không hợp lệ"));

        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        User updated = userRepository.save(user);

        log.info("Admin [{}] đã cập nhật quyền tài khoản [{}] sang: {}", currentAdminUsername, user.getUsername(), targetRole);
        return mapToAdminUserResponse(updated);
    }

    @Override
    @Transactional
    public void resetPassword(Integer idUser, ResetPasswordAdminRequest request) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + idUser));

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);
        log.info("Admin đã đặt lại mật khẩu cho tài khoản: {}", user.getUsername());
    }

    private String normalizeRoleName(String input) {
        if (input == null) return "STAFF";
        String r = input.trim().toUpperCase();
        if (r.equals("OFFLINE_STAFF") || r.equals("POS")) return "STAFF";
        if (r.equals("REMOTE_CSKH") || r.equals("CSKH")) return "ONLINE_STAFF";
        if (r.equals("WAREHOUSE") || r.equals("KHO")) return "WAREHOUSE_MANAGER";
        return r;
    }

    private AdminUserResponse mapToAdminUserResponse(User u) {
        List<String> roleNames = u.getRoles() != null
                ? u.getRoles().stream().map(Role::getNameRole).collect(Collectors.toList())
                : Collections.emptyList();

        String primaryRole = "CUSTOMER";
        String primaryRoleName = "Khách hàng mua sắm";

        if (roleNames.contains("ADMIN")) {
            primaryRole = "ADMIN";
            primaryRoleName = "Quản trị viên cấp cao";
        } else if (roleNames.contains("WAREHOUSE_MANAGER")) {
            primaryRole = "WAREHOUSE_MANAGER";
            primaryRoleName = "Quản lý Kho & Đóng gói (App Kho)";
        } else if (roleNames.contains("STAFF")) {
            primaryRole = "OFFLINE_STAFF";
            primaryRoleName = "Nhân viên Thu ngân (App POS)";
        } else if (roleNames.contains("ONLINE_STAFF")) {
            primaryRole = "REMOTE_CSKH";
            primaryRoleName = "CTV CSKH & Xử lý khiếu nại tại nhà";
        }

        return AdminUserResponse.builder()
                .idUser(u.getIdUser())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .avatar(u.getAvatar())
                .enabled(u.getEnabled() != null ? u.getEnabled() : true)
                .createdAt(u.getCreatedAt())
                .roles(roleNames)
                .primaryRole(primaryRole)
                .primaryRoleName(primaryRoleName)
                .build();
    }
}
