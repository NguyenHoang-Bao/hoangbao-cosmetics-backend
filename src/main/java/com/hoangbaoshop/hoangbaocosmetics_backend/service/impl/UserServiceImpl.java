package com.hoangbaoshop.hoangbaocosmetics_backend.service.impl;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.ChangePasswordRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.user.UpdateProfileRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user.UserProfileResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        User user = findUserByUsername(username);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUserByUsername(username);

        // 1. Kiểm tra họ và tên
        user.setFullName(request.getFullName().trim());

        // 2. Kiểm tra số điện thoại (nếu thay đổi)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String newPhone = request.getPhoneNumber().trim();
            if (!Objects.equals(user.getPhoneNumber(), newPhone)) {
                if (userRepository.existsByPhoneNumber(newPhone)) {
                    throw new BadRequestException("Số điện thoại này đã được sử dụng bởi một tài khoản khác!");
                }
                user.setPhoneNumber(newPhone);
            }
        } else {
            user.setPhoneNumber(null);
        }

        // 3. Kiểm tra email (nếu thay đổi)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!Objects.equals(user.getEmail(), newEmail)) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new BadRequestException("Email này đã được sử dụng bởi một tài khoản khác!");
                }
                user.setEmail(newEmail);
            }
        }

        // 4. Cập nhật avatar (nếu có)
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }

        User updatedUser = userRepository.save(user);
        log.info("Người dùng '{}' đã cập nhật thông tin cá nhân thành công.", username);
        return mapToProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findUserByUsername(username);

        // Kiểm tra loại tài khoản đăng nhập bên thứ 3 (Google/Facebook)
        if (!"LOCAL".equalsIgnoreCase(user.getAuthProvider())) {
            throw new BadRequestException("Tài khoản đăng nhập bằng " + user.getAuthProvider() + " không sử dụng mật khẩu hệ thống.");
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác!");
        }

        // Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        // Kiểm tra mật khẩu xác nhận
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp với mật khẩu mới!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Người dùng '{}' đã đổi mật khẩu thành công.", username);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng: " + username));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getNameRole)
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .loyaltyPoints(user.getLoyaltyPoints())
                .authProvider(user.getAuthProvider())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .roles(roles)
                .build();
    }
}
