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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // [POST] đăng ký tài khoản mới
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Đăng ký tài khoản thành công!"));
    }

    // [POST] đăng nhập bằng username / password
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công!"));
    }

    // [POST] đăng nhập bằng Google OAuth
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập Google thành công!"));
    }

    // [POST] đăng nhập bằng Facebook OAuth
    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithFacebook(@Valid @RequestBody FacebookLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập Facebook thành công!"));
    }

    // [POST] gửi mã OTP đặt lại mật khẩu qua email
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<String>> sendForgotPasswordOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Nếu email tồn tại trên hệ thống, mã OTP đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư!"));
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
                        color: #1D1D1F;
                        line-height: 1.65;
                    }
                    .card {
                        background: #FFFFFF;
                        max-width: 480px;
                        margin: 20px;
                        padding: 48px 40px;
                        border-radius: 18px;
                        box-shadow: 0 4px 24px rgba(0,0,0,0.06);
                        text-align: center;
                    }
                    .icon {
                        width: 64px; height: 64px;
                        margin: 0 auto 20px;
                        background: #E8F5E9;
                        color: #2E7D32;
                        border-radius: 50%;
                        display: flex; align-items: center; justify-content: center;
                        font-size: 32px;
                    }
                    h1 { font-size: 24px; font-weight: 700; margin: 0 0 12px 0; line-height: 1.35; }
                    p { font-size: 15px; color: #86868B; line-height: 1.65; margin: 0 0 28px 0; }
                    .btn {
                        display: inline-block;
                        background-color: #0071E3;
                        color: #FFFFFF;
                        text-decoration: none;
                        padding: 13px 32px;
                        border-radius: 980px;
                        font-size: 15px;
                        font-weight: 500;
                    }
                    .btn:hover { background-color: #0077ED; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">✓</div>
                    <h1>Xác Nhận Email Thành Công</h1>
                    <p>Địa chỉ email của bạn đã được xác minh thành công. Bạn đã sẵn sàng trải nghiệm mua sắm tuyệt vời tại Hoàng Bảo Cosmetics!</p>
                    <a href="%s" class="btn">Tiếp Tục Mua Sắm</a>
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
                <title>Xác Thực Thất Bại - Hoàng Bảo Cosmetics</title>
                <style>
                    body {
                        margin: 0; padding: 0;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        display: flex; align-items: center; justify-content: center;
                        min-height: 100vh;
                        color: #1D1D1F;
                        line-height: 1.65;
                    }
                    .card {
                        background: #FFFFFF;
                        max-width: 480px;
                        margin: 20px;
                        padding: 48px 40px;
                        border-radius: 18px;
                        box-shadow: 0 4px 24px rgba(0,0,0,0.06);
                        text-align: center;
                    }
                    .icon {
                        width: 64px; height: 64px;
                        margin: 0 auto 20px;
                        background: #FFEBEE;
                        color: #C62828;
                        border-radius: 50%;
                        display: flex; align-items: center; justify-content: center;
                        font-size: 32px;
                    }
                    h1 { font-size: 24px; font-weight: 700; margin: 0 0 12px 0; line-height: 1.35; }
                    p { font-size: 15px; color: #86868B; line-height: 1.65; margin: 0 0 28px 0; }
                    .btn {
                        display: inline-block;
                        background-color: #1D1D1F;
                        color: #FFFFFF;
                        text-decoration: none;
                        padding: 13px 32px;
                        border-radius: 980px;
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

