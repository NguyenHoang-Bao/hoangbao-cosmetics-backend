package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.SendOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.VerifyOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.ResetPasswordWithOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Các API xác thực, đăng nhập tài khoản thường, Google/Facebook OAuth2, và OTP")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // [POST] đăng ký tài khoản mới
    @Operation(summary = "Đăng ký tài khoản người dùng mới (Public)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Đăng ký tài khoản thành công!"));
    }

    // [POST] đăng nhập bằng username / password
    @Operation(summary = "Đăng nhập bằng tài khoản và mật khẩu (Public)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công!"));
    }

    // [POST] đăng nhập bằng Google OAuth
    @Operation(summary = "Đăng nhập bằng tài khoản Google (OAuth 2.0 / OpenID Connect)", 
               description = "Xác thực Google ID Token hoặc Access Token từ Google SDK và cấp JWT Token cho người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập Google thành công, trả về JWT Token và thông tin User"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token Google không hợp lệ hoặc tài khoản bị xung đột")
    })
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập Google thành công!"));
    }

    // [POST] đăng nhập bằng Facebook OAuth
    @Operation(summary = "Đăng nhập bằng tài khoản Facebook (OAuth 2.0)", 
               description = "Xác thực Facebook User Access Token từ Facebook SDK và cấp JWT Token cho người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập Facebook thành công, trả về JWT Token và thông tin User"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token Facebook không hợp lệ hoặc tài khoản bị xung đột")
    })
    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithFacebook(@Valid @RequestBody FacebookLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập Facebook thành công!"));
    }

    // [POST] gửi mã OTP đặt lại mật khẩu qua email
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<String>> sendForgotPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mã OTP đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư!"));
    }

    // [POST] xác thực mã OTP đặt lại mật khẩu
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Xác thực mã OTP thành công!"));
    }

    // [POST] đặt lại mật khẩu mới bằng mã OTP
    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPasswordWithOtp(@Valid @RequestBody ResetPasswordWithOtpRequest request) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ."));
    }

    // [GET] xác thực email 1-click từ liên kết trong email
    @GetMapping(value = "/verify-email", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            authService.verifyEmailToken(token);
            return ResponseEntity.ok(buildVerificationSuccessHtml());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildVerificationErrorHtml(e.getMessage()));
        }
    }

    // [POST] đăng xuất tài khoản và đưa JWT vào Redis blacklist
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công!"));
    }

    private String buildVerificationSuccessHtml() {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác Nhận Email Thành Công - Hoàng Bảo Cosmetics</title>
                <style>
                    body {
                        margin: 0; padding: 0;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        display: flex; align-items: center; justify-content: center;
                        min-height: 100vh;
                    }
                    .card {
                        background: #FFFFFF;
                        border-radius: 16px;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                        max-width: 440px;
                        width: 90%;
                        padding: 40px 32px;
                        text-align: center;
                    }
                    .icon {
                        width: 64px; height: 64px;
                        background-color: #E8F5E9;
                        color: #2E7D32;
                        border-radius: 50%;
                        display: inline-flex;
                        align-items: center; justify-content: center;
                        font-size: 32px;
                        margin-bottom: 20px;
                    }
                    h1 {
                        font-size: 22px;
                        color: #1A1A1A;
                        margin: 0 0 12px 0;
                        font-weight: 700;
                    }
                    p {
                        font-size: 14px;
                        color: #666666;
                        line-height: 1.6;
                        margin: 0 0 28px 0;
                    }
                    .btn {
                        display: inline-block;
                        width: 100%;
                        padding: 12px 0;
                        background: linear-gradient(135deg, #10B981 0%, #059669 100%);
                        color: #FFFFFF;
                        border-radius: 8px;
                        text-decoration: none;
                        font-size: 15px;
                        font-weight: 600;
                        transition: opacity 0.2s;
                    }
                    .btn:hover { opacity: 0.9; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">✓</div>
                    <h1>Xác Thực Email Thành Công!</h1>
                    <p>Tài khoản Hoàng Bảo Cosmetics của bạn đã được kích hoạt. Hãy đăng nhập để bắt đầu mua sắm ngay hôm nay.</p>
                    <a href="%s/login" class="btn">Đăng Nhập Ngay</a>
                </div>
            </body>
            </html>
            """.formatted(frontendUrl);
    }

    private String buildVerificationErrorHtml(String errorMessage) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Lỗi Xác Nhận Email - Hoàng Bảo Cosmetics</title>
                <style>
                    body {
                        margin: 0; padding: 0;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        display: flex; align-items: center; justify-content: center;
                        min-height: 100vh;
                    }
                    .card {
                        background: #FFFFFF;
                        border-radius: 16px;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
                        max-width: 440px;
                        width: 90%;
                        padding: 40px 32px;
                        text-align: center;
                    }
                    .icon {
                        width: 64px; height: 64px;
                        background-color: #FFEBEE;
                        color: #C62828;
                        border-radius: 50%;
                        display: inline-flex;
                        align-items: center; justify-content: center;
                        font-size: 32px;
                        margin-bottom: 20px;
                    }
                    h1 {
                        font-size: 22px;
                        color: #1A1A1A;
                        margin: 0 0 12px 0;
                        font-weight: 700;
                    }
                    p {
                        font-size: 14px;
                        color: #666666;
                        line-height: 1.6;
                        margin: 0 0 28px 0;
                    }
                    .btn {
                        display: inline-block;
                        width: 100%;
                        padding: 12px 0;
                        background-color: #6B7280;
                        color: #FFFFFF;
                        border-radius: 8px;
                        text-decoration: none;
                        font-size: 15px;
                        font-weight: 500;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">✕</div>
                    <h1>Xác Thực Không Thành Công</h1>
                    <p>%s</p>
                    <a href="%s" class="btn">Quay Về Trang Chủ</a>
                </div>
            </body>
            </html>
            """.formatted(
                errorMessage != null ? errorMessage : "Liên kết không hợp lệ hoặc đã hết hạn.",
                frontendUrl
            );
    }
}
