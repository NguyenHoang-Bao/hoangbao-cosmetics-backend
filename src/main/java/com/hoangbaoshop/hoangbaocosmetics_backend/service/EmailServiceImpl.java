package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.email.OrderEmailDto;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:hbao4142@gmail.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    // gửi mã xác thực OTP qua email bất đồng bộ
    @Override
    @Async("mailExecutor")
    public void sendOtpEmail(String toEmail, String otpCode, OtpTokenType tokenType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Hoàng Bảo Cosmetics");
            helper.setTo(toEmail);

            String subject = tokenType == OtpTokenType.FORGOT_PASSWORD
                    ? "[Hoàng Bảo Cosmetics] Mã xác thực đặt lại mật khẩu"
                    : "[Hoàng Bảo Cosmetics] Mã xác thực tài khoản";
            helper.setSubject(subject);

            String title = tokenType == OtpTokenType.FORGOT_PASSWORD
                    ? "Yêu Cầu Đặt Lại Mật Khẩu"
                    : "Xác Thực Tài Khoản Mới";

            String actionText = tokenType == OtpTokenType.FORGOT_PASSWORD
                    ? "Bạn đang thực hiện đặt lại mật khẩu cho tài khoản tại <strong>Hoàng Bảo Cosmetics</strong>."
                    : "Cảm ơn bạn đã đăng ký tài khoản tại <strong>Hoàng Bảo Cosmetics</strong>.";

            String htmlContent = buildOtpEmailHtml(title, actionText, otpCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email OTP thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email OTP thất bại tới {}: {}", toEmail, e.getMessage());
        }
    }

    // gửi email xác thực tài khoản 1-click khi đăng ký mới bất đồng bộ
    @Override
    @Async("mailExecutor")
    public void sendAccountVerificationEmail(String toEmail, String fullName, String verificationUrl) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Hoàng Bảo Cosmetics");
            helper.setTo(toEmail);
            helper.setSubject("[Hoàng Bảo Cosmetics] Xác nhận địa chỉ email của bạn");

            String htmlContent = buildAccountVerificationEmailHtml(fullName != null ? fullName : "Quý khách", verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác thực tài khoản 1-click thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email xác thực tài khoản thất bại tới {}: {}", toEmail, e.getMessage());
        }
    }

    // gửi email thông báo xác nhận đặt hàng thành công bất đồng bộ
    @Override
    @Async("mailExecutor")
    public void sendOrderConfirmationEmail(OrderEmailDto order, String toEmail) {
        if (toEmail == null || toEmail.isBlank() || order == null) {
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Hoàng Bảo Cosmetics");
            helper.setTo(toEmail);
            helper.setSubject("[Hoàng Bảo Cosmetics] Xác nhận đơn hàng #" + order.getOrderCode());

            String htmlContent = buildOrderEmailHtml(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác nhận đơn hàng #{} thành công tới: {}", order.getOrderCode(), toEmail);
        } catch (Exception e) {
            log.error("Gửi email xác nhận đơn hàng #{} thất bại tới {}: {}", order.getOrderCode(), toEmail, e.getMessage());
        }
    }

    // hàm tạo nội dung HTML cho email OTP (phong cách Apple tối giản sang trọng)
    private String buildOtpEmailHtml(String title, String actionText, String otpCode) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html {
                        margin: 0;
                        padding: 0;
                        width: 100%%;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                        line-height: 1.65;
                    }

                    .email-container {
                        width: 100%%;
                        padding: 48px 16px;
                        box-sizing: border-box;
                        background-color: #F5F5F7;
                    }

                    .email-card {
                        max-width: 540px;
                        margin: 0 auto;
                        background-color: #FFFFFF;
                        border-radius: 18px;
                        padding: 48px 44px;
                        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
                        box-sizing: border-box;
                    }

                    .brand-header {
                        text-align: center;
                        margin-bottom: 32px;
                    }

                    .brand-name {
                        font-size: 11px;
                        font-weight: 600;
                        color: #86868B;
                        text-transform: uppercase;
                        letter-spacing: 0.12em;
                        margin: 0;
                    }

                    .main-content {
                        text-align: center;
                    }

                    .title {
                        font-size: 26px;
                        font-weight: 700;
                        color: #1D1D1F;
                        margin: 0 0 16px 0;
                        line-height: 1.35;
                    }

                    .text {
                        font-size: 15px;
                        line-height: 1.65;
                        color: #1D1D1F;
                        margin: 0 0 10px 0;
                    }

                    .otp-box {
                        background-color: #F5F5F7;
                        border-radius: 14px;
                        padding: 28px 16px;
                        margin: 32px 0;
                        text-align: center;
                    }

                    .otp-label {
                        font-size: 11px;
                        font-weight: 600;
                        color: #86868B;
                        text-transform: uppercase;
                        letter-spacing: 0.1em;
                        margin-bottom: 12px;
                    }

                    .otp-code {
                        font-family: 'SF Mono', SFMono-Regular, ui-monospace, Menlo, Monaco, Consolas, monospace;
                        font-size: 42px;
                        font-weight: 700;
                        color: #1D1D1F;
                        letter-spacing: 0.25em;
                        margin: 0;
                        margin-left: 0.25em;
                    }

                    .expiration {
                        font-size: 13px;
                        color: #86868B;
                        margin-top: 12px;
                        font-weight: 400;
                        line-height: 1.5;
                    }

                    .security-note {
                        font-size: 13px;
                        line-height: 1.65;
                        color: #86868B;
                        text-align: center;
                        margin: 0;
                    }

                    .footer {
                        margin-top: 40px;
                        padding-top: 24px;
                        border-top: 1px solid #D2D2D7;
                        text-align: center;
                    }

                    .footer p {
                        font-size: 12px;
                        color: #86868B;
                        margin: 0 0 6px 0;
                        line-height: 1.5;
                    }

                    .footer-link {
                        color: #0066CC;
                        text-decoration: none;
                    }

                    @media only screen and (max-width: 480px) {
                        .email-card {
                            padding: 40px 24px;
                        }
                        .otp-code {
                            font-size: 34px;
                            letter-spacing: 0.2em;
                            margin-left: 0.2em;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-card">
                        <div class="brand-header">
                            <p class="brand-name">Hoàng Bảo Cosmetics</p>
                        </div>
                        <div class="main-content">
                            <h1 class="title">%s</h1>
                            <p class="text">%s</p>
                            <div class="otp-box">
                                <div class="otp-label">Mã xác thực</div>
                                <div class="otp-code">%s</div>
                                <div class="expiration">Mã này sẽ hết hạn trong 5 phút.</div>
                            </div>
                            <p class="security-note">
                                Không chia sẻ mã này cho bất kỳ ai. Nếu bạn không yêu cầu mã này, bạn có thể bỏ qua email một cách an toàn.
                            </p>
                        </div>
                        <div class="footer">
                            <p>Bản quyền © 2026 Hoàng Bảo Cosmetics.</p>
                            <p>Hotline: 0359 490 221 | <a href="mailto:support@hoangbaocosmetics.vn" class="footer-link">support@hoangbaocosmetics.vn</a></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, actionText, otpCode);
    }

    // hàm tạo nội dung HTML cho email xác thực tài khoản 1-click (chuẩn giao diện Apple)
    private String buildAccountVerificationEmailHtml(String fullName, String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html {
                        margin: 0;
                        padding: 0;
                        width: 100%%;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                        line-height: 1.65;
                    }

                    .email-container {
                        width: 100%%;
                        padding: 48px 16px;
                        box-sizing: border-box;
                        background-color: #F5F5F7;
                    }

                    .email-card {
                        max-width: 540px;
                        margin: 0 auto;
                        background-color: #FFFFFF;
                        border-radius: 18px;
                        padding: 48px 44px;
                        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
                        box-sizing: border-box;
                    }

                    .brand-header {
                        text-align: center;
                        margin-bottom: 32px;
                    }

                    .brand-name {
                        font-size: 11px;
                        font-weight: 600;
                        color: #86868B;
                        text-transform: uppercase;
                        letter-spacing: 0.12em;
                        margin: 0;
                    }

                    .main-content {
                        text-align: center;
                    }

                    .title {
                        font-size: 26px;
                        font-weight: 700;
                        color: #1D1D1F;
                        margin: 0 0 16px 0;
                        line-height: 1.35;
                    }

                    .text {
                        font-size: 15px;
                        line-height: 1.65;
                        color: #1D1D1F;
                        margin: 0 0 12px 0;
                    }

                    .button-box {
                        margin: 36px 0;
                        text-align: center;
                    }

                    .verify-button {
                        background-color: #0071E3;
                        color: #FFFFFF !important;
                        font-size: 15px;
                        font-weight: 500;
                        text-decoration: none;
                        padding: 14px 36px;
                        border-radius: 980px;
                        display: inline-block;
                        box-shadow: 0 2px 8px rgba(0, 113, 227, 0.25);
                    }

                    .security-note {
                        font-size: 13px;
                        line-height: 1.65;
                        color: #86868B;
                        text-align: center;
                        margin: 24px 0 0 0;
                    }

                    .footer {
                        margin-top: 40px;
                        padding-top: 24px;
                        border-top: 1px solid #D2D2D7;
                        text-align: center;
                    }

                    .footer p {
                        font-size: 12px;
                        color: #86868B;
                        margin: 0 0 6px 0;
                        line-height: 1.5;
                    }

                    .footer-link {
                        color: #0066CC;
                        text-decoration: none;
                    }

                    @media only screen and (max-width: 480px) {
                        .email-card {
                            padding: 40px 24px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-card">
                        <div class="brand-header">
                            <p class="brand-name">Hoàng Bảo Cosmetics</p>
                        </div>
                        <div class="main-content">
                            <h1 class="title">Chào Mừng Bạn</h1>
                            <p class="text">Xin chào <strong>%s</strong>,</p>
                            <p class="text">Cảm ơn bạn đã đăng ký tài khoản tại Hoàng Bảo Cosmetics. Vui lòng nhấn vào nút bên dưới để xác minh email và kích hoạt trọn vẹn đặc quyền thành viên:</p>
                            <div class="button-box">
                                <a href="%s" class="verify-button">Xác nhận địa chỉ Email</a>
                            </div>
                            <p class="security-note">
                                Liên kết xác nhận có hiệu lực trong vòng 24 giờ. Nếu bạn không tạo tài khoản này, bạn có thể an tâm bỏ qua email này.
                            </p>
                        </div>
                        <div class="footer">
                            <p>Bản quyền © 2026 Hoàng Bảo Cosmetics.</p>
                            <p>Hotline: 0359 490 221 | <a href="mailto:support@hoangbaocosmetics.vn" class="footer-link">support@hoangbaocosmetics.vn</a></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationUrl);
    }

    // hàm tạo nội dung HTML cho email thông báo đơn hàng (chuẩn phong cách Apple tối giản sang trọng)
    private String buildOrderEmailHtml(OrderEmailDto order) {
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderEmailDto.Item item : order.getItems()) {
                String name = item.getProductName() != null ? item.getProductName() : "Sản phẩm";
                String capacity = (item.getCapacity() != null && !item.getCapacity().isBlank()) ? " (" + item.getCapacity() + ")" : "";
                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal() : price.multiply(BigDecimal.valueOf(item.getQuantity()));

                itemsHtml.append("""
                    <tr>
                        <td style="padding: 14px 8px; border-bottom: 1px solid #E5E5EA; color: #1D1D1F; font-weight: 500; line-height: 1.5;">%s%s</td>
                        <td style="padding: 14px 8px; border-bottom: 1px solid #E5E5EA; text-align: center; color: #86868B; white-space: nowrap;">%d</td>
                        <td style="padding: 14px 8px; border-bottom: 1px solid #E5E5EA; text-align: right; color: #86868B; white-space: nowrap;">%s đ</td>
                        <td style="padding: 14px 8px; border-bottom: 1px solid #E5E5EA; text-align: right; font-weight: 600; color: #1D1D1F; white-space: nowrap;">%s đ</td>
                    </tr>
                """.formatted(
                        name,
                        capacity,
                        item.getQuantity(),
                        CURRENCY_FORMAT.format(price),
                        CURRENCY_FORMAT.format(lineTotal)
                ));
            }
        }

        String createdAtStr = order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
        String shippingFeeStr = order.getShippingFee() != null ? CURRENCY_FORMAT.format(order.getShippingFee()) : "0";
        String totalAmountStr = order.getTotalAmount() != null ? CURRENCY_FORMAT.format(order.getTotalAmount()) : "0";

        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html {
                        margin: 0;
                        padding: 0;
                        width: 100%%;
                        background-color: #F5F5F7;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                        line-height: 1.65;
                    }

                    .email-container {
                        width: 100%%;
                        padding: 48px 16px;
                        box-sizing: border-box;
                        background-color: #F5F5F7;
                    }

                    .email-card {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #FFFFFF;
                        border-radius: 18px;
                        padding: 44px 40px;
                        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
                        box-sizing: border-box;
                    }

                    .brand-header {
                        text-align: center;
                        margin-bottom: 28px;
                    }

                    .brand-name {
                        font-size: 11px;
                        font-weight: 600;
                        color: #86868B;
                        text-transform: uppercase;
                        letter-spacing: 0.12em;
                        margin: 0;
                    }

                    .main-content {
                        text-align: left;
                    }

                    .title {
                        font-size: 26px;
                        font-weight: 700;
                        color: #1D1D1F;
                        margin: 0 0 10px 0;
                        text-align: center;
                        line-height: 1.35;
                    }

                    .text {
                        font-size: 15px;
                        line-height: 1.65;
                        color: #1D1D1F;
                        margin: 0 0 20px 0;
                        text-align: center;
                    }

                    .info-box {
                        background-color: #F5F5F7;
                        border-radius: 14px;
                        padding: 24px 26px;
                        margin: 24px 0;
                    }

                    .info-table {
                        width: 100%%;
                        border-collapse: collapse;
                        font-size: 13px;
                        line-height: 1.6;
                    }

                    .info-label {
                        color: #86868B;
                        padding: 6px 0;
                        width: 36%%;
                        vertical-align: top;
                    }

                    .info-value {
                        color: #1D1D1F;
                        font-weight: 600;
                        padding: 6px 0;
                        vertical-align: top;
                    }

                    .items-title {
                        font-size: 15px;
                        font-weight: 600;
                        color: #1D1D1F;
                        margin: 30px 0 12px 0;
                    }

                    .product-table {
                        width: 100%%;
                        border-collapse: collapse;
                        font-size: 13px;
                    }

                    .product-table th {
                        font-size: 11px;
                        font-weight: 600;
                        color: #86868B;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                        padding: 12px 8px;
                        border-bottom: 1px solid #D2D2D7;
                        text-align: left;
                    }

                    .summary-box {
                        margin-top: 24px;
                        padding-top: 16px;
                        border-top: 1px solid #D2D2D7;
                        text-align: right;
                        font-size: 13px;
                    }

                    .summary-row {
                        padding: 5px 0;
                        color: #86868B;
                        line-height: 1.6;
                    }

                    .grand-total {
                        font-size: 20px;
                        font-weight: 700;
                        color: #1D1D1F;
                        margin-top: 10px;
                    }

                    .footer {
                        margin-top: 36px;
                        padding-top: 24px;
                        border-top: 1px solid #D2D2D7;
                        text-align: center;
                    }

                    .footer p {
                        font-size: 12px;
                        color: #86868B;
                        margin: 0 0 6px 0;
                        line-height: 1.5;
                    }

                    .footer-link {
                        color: #0066CC;
                        text-decoration: none;
                    }

                    @media only screen and (max-width: 520px) {
                        .email-card {
                            padding: 36px 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-card">
                        <div class="brand-header">
                            <p class="brand-name">Hoàng Bảo Cosmetics</p>
                        </div>
                        <div class="main-content">
                            <h1 class="title">Xác Nhận Đơn Hàng</h1>
                            <p class="text">Xin chào <strong>%s</strong>, đơn hàng của bạn đã được hệ thống tiếp nhận thành công.</p>
                            
                            <div class="info-box">
                                <table class="info-table">
                                    <tr>
                                        <td class="info-label">Mã đơn hàng:</td>
                                        <td class="info-value">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="info-label">Thời gian đặt:</td>
                                        <td class="info-value" style="font-weight: 400;">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="info-label">Số điện thoại:</td>
                                        <td class="info-value" style="font-weight: 400;">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="info-label">Địa chỉ giao hàng:</td>
                                        <td class="info-value" style="font-weight: 400;">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="info-label">Phương thức thanh toán:</td>
                                        <td class="info-value">%s</td>
                                    </tr>
                                </table>
                            </div>

                            <div class="items-title">Chi tiết sản phẩm</div>
                            <table class="product-table">
                                <thead>
                                    <tr>
                                        <th>Sản phẩm</th>
                                        <th style="text-align: center;">SL</th>
                                        <th style="text-align: right;">Đơn giá</th>
                                        <th style="text-align: right;">Tạm tính</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>

                            <div class="summary-box">
                                <div class="summary-row">Phí vận chuyển: <strong style="color: #1D1D1F;">%s đ</strong></div>
                                <div class="grand-total">Tổng thanh toán: %s đ</div>
                            </div>
                        </div>
                        <div class="footer">
                            <p>Bản quyền © 2026 Hoàng Bảo Cosmetics. Cam kết chính hãng 100%%.</p>
                            <p>Hotline CSKH: 0359 490 221 | <a href="mailto:support@hoangbaocosmetics.vn" class="footer-link">support@hoangbaocosmetics.vn</a></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                order.getCustomerName() != null ? order.getCustomerName() : "Quý khách",
                order.getOrderCode(),
                createdAtStr,
                order.getPhoneNumber() != null ? order.getPhoneNumber() : "---",
                order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Tại cửa hàng",
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD",
                itemsHtml.toString(),
                shippingFeeStr,
                totalAmountStr
        );
    }
}
