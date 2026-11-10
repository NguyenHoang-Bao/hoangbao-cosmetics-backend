package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.CreateStaffRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ResetPasswordAdminRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.AdminUserResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @Test
    @DisplayName("Lấy danh sách người dùng phân trang và lọc thành công")
    void getAllUsers_Success() {
        Role staffRole = Role.builder().idRole(3).nameRole("STAFF").build();
        User user1 = User.builder()
                .idUser(2)
                .username("thungan_pos")
                .fullName("Trần Thị Thu Ngân")
                .email("pos@hoangbao.vn")
                .enabled(true)
                .roles(new HashSet<>(Collections.singletonList(staffRole)))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.singletonList(user1)));

        PageResponse<AdminUserResponse> response = adminUserService.getAllUsers("Thu Ngân", "STAFF", true, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Trần Thị Thu Ngân", response.getContent().get(0).getFullName());
        assertEquals("OFFLINE_STAFF", response.getContent().get(0).getPrimaryRole());
    }

    @Test
    @DisplayName("Tạo mới tài khoản nhân viên thành công")
    void createStaff_Success() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .username("thukho_lan")
                .fullName("Lê Văn Kho")
                .email("kho@hoangbao.vn")
                .phoneNumber("0912345678")
                .password("MatKhau123")
                .roleName("WAREHOUSE_MANAGER")
                .build();

        Role role = Role.builder().idRole(2).nameRole("WAREHOUSE_MANAGER").build();

        when(userRepository.existsByUsername("thukho_lan")).thenReturn(false);
        when(userRepository.existsByEmail("kho@hoangbao.vn")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("0912345678")).thenReturn(false);
        when(roleRepository.findByNameRole("WAREHOUSE_MANAGER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("MatKhau123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setIdUser(5);
            return u;
        });

        AdminUserResponse response = adminUserService.createStaff(request);

        assertNotNull(response);
        assertEquals("thukho_lan", response.getUsername());
        assertEquals("Lê Văn Kho", response.getFullName());
        assertTrue(response.getRoles().contains("WAREHOUSE_MANAGER"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Báo lỗi khi tạo nhân viên với email đã tồn tại")
    void createStaff_DuplicateEmail_ThrowsException() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .username("new_staff")
                .fullName("Nguyễn Văn A")
                .email("existed@hoangbao.vn")
                .roleName("STAFF")
                .build();

        when(userRepository.existsByUsername("new_staff")).thenReturn(false);
        when(userRepository.existsByEmail("existed@hoangbao.vn")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminUserService.createStaff(request));
        assertTrue(ex.getMessage().contains("đã tồn tại trong hệ thống"));
    }

    @Test
    @DisplayName("Khóa tài khoản nhân viên thành công")
    void toggleUserStatus_Success() {
        User user = User.builder()
                .idUser(5)
                .username("staff_pos")
                .enabled(true)
                .roles(Collections.emptySet())
                .build();

        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AdminUserResponse response = adminUserService.toggleUserStatus(5, "admin");

        assertNotNull(response);
        assertFalse(response.getEnabled());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Chặn không cho phép khóa tài khoản Admin gốc (ID 1)")
    void toggleUserStatus_RootAdmin_ThrowsException() {
        User rootAdmin = User.builder()
                .idUser(1)
                .username("admin")
                .enabled(true)
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(rootAdmin));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> adminUserService.toggleUserStatus(1, "admin"));
        assertTrue(ex.getMessage().contains("Không thể khóa tài khoản"));
    }

    @Test
    @DisplayName("Admin đặt lại mật khẩu thành công")
    void resetPassword_Success() {
        User user = User.builder()
                .idUser(3)
                .username("pos_staff")
                .password("old_pass")
                .build();

        when(userRepository.findById(3)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@456")).thenReturn("encoded_new_pass");

        ResetPasswordAdminRequest req = ResetPasswordAdminRequest.builder()
                .newPassword("NewPass@456")
                .build();

        adminUserService.resetPassword(3, req);

        assertEquals("encoded_new_pass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }
}
