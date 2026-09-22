package com.hoangbaoshop.hoangbaocosmetics_backend.controller;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Đăng ký tài khoản thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công!"));
    }
}

