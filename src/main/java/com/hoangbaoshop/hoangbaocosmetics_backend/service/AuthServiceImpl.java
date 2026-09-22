package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.JwtUtils;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra dữ liệu đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }

        // 2. Tạo đối tượng User mới và mã hóa mật khẩu
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .build();

        // 3. Gán Role mặc định là CUSTOMER cho người dùng mới đăng ký
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByNameRole("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role CUSTOMER trong Database."));
        roles.add(userRole);
        user.setRoles(roles);

        // 4. Lưu vào Database
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực tài khoản qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Đặt thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Sinh ra chuỗi JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. Lấy thông tin user hiện tại
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        // 5. Trả về Response cho Frontend
        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .idUser(userDetails.getIdUser())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .avatar(userDetails.getAvatar())
                .roles(roles)
                .build();
    }
}