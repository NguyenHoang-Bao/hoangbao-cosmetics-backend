package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.email.OrderEmailDto;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "hbao4142@gmail.com");
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Gửi OTP email thành công bằng Thymeleaf")
    void testSendOtpEmail_Success() {
        when(templateEngine.process(eq("emails/otp-mail"), any(Context.class))).thenReturn("<html>OTP</html>");

        emailService.sendOtpEmail("test@example.com", "123456", OtpTokenType.FORGOT_PASSWORD);

        verify(templateEngine, times(1)).process(eq("emails/otp-mail"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Bỏ qua gửi email xác thực tài khoản nếu toEmail rỗng")
    void testSendAccountVerificationEmail_EmptyEmail() {
        emailService.sendAccountVerificationEmail("", "Nguyễn Văn A", "http://example.com/verify");
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Gửi email xác nhận tài khoản 1-click thành công")
    void testSendAccountVerificationEmail_Success() {
        when(templateEngine.process(eq("emails/account-verification"), any(Context.class))).thenReturn("<html>Verify</html>");

        emailService.sendAccountVerificationEmail("test@example.com", "Nguyễn Văn A", "http://example.com/verify");

        verify(templateEngine, times(1)).process(eq("emails/account-verification"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Gửi email xác nhận đơn hàng thành công")
    void testSendOrderConfirmationEmail_Success() {
        when(templateEngine.process(eq("emails/order-confirmation"), any(Context.class))).thenReturn("<html>Order</html>");

        OrderEmailDto dto = OrderEmailDto.builder()
                .orderCode("HB20261023001")
                .customerName("Khách hàng")
                .phoneNumber("0901234567")
                .deliveryAddress("Hà Nội")
                .paymentMethod("COD")
                .createdAt(LocalDateTime.now())
                .shippingFee(BigDecimal.valueOf(30000))
                .totalAmount(BigDecimal.valueOf(300000))
                .items(List.of(
                        OrderEmailDto.Item.builder()
                                .productName("Kem chống nắng")
                                .capacity("50ml")
                                .quantity(1)
                                .price(BigDecimal.valueOf(270000))
                                .lineTotal(BigDecimal.valueOf(270000))
                                .build()
                ))
                .build();

        emailService.sendOrderConfirmationEmail(dto, "test@example.com");

        verify(templateEngine, times(1)).process(eq("emails/order-confirmation"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("CurrencyFormatter định dạng số tiền VND chính xác")
    void testCurrencyFormatter() {
        EmailServiceImpl.CurrencyFormatter formatter = new EmailServiceImpl.CurrencyFormatter();
        org.junit.jupiter.api.Assertions.assertEquals("0", formatter.format(null));
        org.junit.jupiter.api.Assertions.assertNotNull(formatter.format(BigDecimal.valueOf(150000)));
    }
}
