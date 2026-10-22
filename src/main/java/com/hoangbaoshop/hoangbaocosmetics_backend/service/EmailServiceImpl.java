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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:hbao4142@gmail.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final CurrencyFormatter CURRENCY_FORMATTER = new CurrencyFormatter();

    // gửi mã xác thực OTP qua email bất đồng bộ bằng Thymeleaf Template
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

            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("actionText", actionText);
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("emails/otp-mail", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email OTP thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email OTP thất bại tới {}: {}", toEmail, e.getMessage());
        }
    }

    // gửi email xác thực tài khoản 1-click khi đăng ký mới bất đồng bộ bằng Thymeleaf Template
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

            Context context = new Context();
            context.setVariable("fullName", fullName != null ? fullName : "Quý khách");
            context.setVariable("verificationUrl", verificationUrl);

            String htmlContent = templateEngine.process("emails/account-verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác thực tài khoản 1-click thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.error("Gửi email xác thực tài khoản thất bại tới {}: {}", toEmail, e.getMessage());
        }
    }

    // gửi email thông báo xác nhận đặt hàng thành công bất đồng bộ bằng Thymeleaf Template
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

            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("formattedCreatedAt", order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_TIME_FORMATTER) : "");
            context.setVariable("formatter", CURRENCY_FORMATTER);

            String htmlContent = templateEngine.process("emails/order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác nhận đơn hàng #{} thành công tới: {}", order.getOrderCode(), toEmail);
        } catch (Exception e) {
            log.error("Gửi email xác nhận đơn hàng #{} thất bại tới {}: {}", order.getOrderCode(), toEmail, e.getMessage());
        }
    }

    public static class CurrencyFormatter {
        private final NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        public String format(BigDecimal amount) {
            return amount != null ? formatter.format(amount) : "0";
        }
    }
}
