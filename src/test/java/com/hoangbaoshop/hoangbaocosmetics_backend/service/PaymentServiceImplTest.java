package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.config.PaymentProperties;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentProperties.Sepay sepayConfig;

    @BeforeEach
    void setUp() {
        sepayConfig = new PaymentProperties.Sepay();
        lenient().when(paymentProperties.getSepay()).thenReturn(sepayConfig);
    }

    @Test
    void processSepayWebhook_WhenApiKeyNotConfigured_RejectsFailClosed() {
        // Given: API key is null or blank (or default placeholder)
        sepayConfig.setApiKey(null);
        SepayWebhookRequest request = new SepayWebhookRequest();

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey test_key");

        // Then: Fail-closed ensures request is rejected
        assertFalse(result);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void processSepayWebhook_WhenApiKeyIsDefaultPlaceholder_RejectsFailClosed() {
        // Given
        sepayConfig.setApiKey("SEPAY_API_KEY_SECRET");
        SepayWebhookRequest request = new SepayWebhookRequest();

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey SEPAY_API_KEY_SECRET");

        // Then
        assertFalse(result);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void processSepayWebhook_WhenInvalidAuthorizationHeader_Rejects() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = new SepayWebhookRequest();

        // When: wrong auth header format or wrong key
        boolean result = paymentService.processSepayWebhook(request, "Apikey wrong_key");

        // Then
        assertFalse(result);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void processSepayWebhook_WhenIdempotentTransactionExists_ReturnsTrueImmediately() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan don HB20261013A1B2C3")
                .referenceCode("TX-999")
                .build();

        when(orderRepository.existsByTransactionId("TX-999")).thenReturn(true);

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then
        assertTrue(result);
        verify(orderRepository, never()).findByOrderCodeForUpdate(any());
    }

    @Test
    void processSepayWebhook_WhenValidPayment_UpdatesOrderToProcessing() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan HB20261013A1B2C3")
                .referenceCode("TX-888")
                .transferAmount(BigDecimal.valueOf(250000))
                .gateway("MBBank")
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261013A1B2C3")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-888")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("HB20261013A1B2C3")).thenReturn(Optional.of(order));

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then
        assertTrue(result);
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(PaymentMethod.BANK_TRANSFER, order.getPaymentMethod());
        assertEquals("TX-888", order.getTransactionId());
        assertEquals("SUCCESS", order.getWebhookStatus());
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void processSepayWebhook_WhenCaseInsensitiveAndSpecialChars_MatchesAndProcessesSuccessfully() {
        // Given: Nội dung chuyển khoản có chữ thường và ký tự thừa: "CK hb-20261013-a1b2c3"
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("CK hb-20261013-a1b2c3 thanh toan")
                .referenceCode("TX-889")
                .transferAmount(BigDecimal.valueOf(250000))
                .gateway("MBBank")
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261013A1B2C3")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-889")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("HB20261013A1B2C3")).thenReturn(Optional.of(order));

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Vẫn trích xuất chính xác mã đơn HB20261013A1B2C3
        assertTrue(result);
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void processSepayWebhook_WhenOrderCodeNotFound_ReturnsTrueToStopRetries() {
        // Given: Giao dịch lạ không tìm thấy đơn
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Chuyen tien mua hang khong ma don")
                .referenceCode("TX-000")
                .build();

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Trả về true để SePay không gửi lại liên tục
        assertTrue(result);
        verify(orderRepository, never()).findByOrderCodeForUpdate(any());
    }

    @Test
    void processSepayWebhook_WhenDuplicatePaymentToProcessingOrder_AppendsRefundNote() {
        // Given: Đơn đã PROCESSING nhưng khách chuyển thêm lần 2
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan HB20261013A1B2C3 lan 2")
                .referenceCode("TX-555")
                .transferAmount(BigDecimal.valueOf(250000))
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261013A1B2C3")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PROCESSING)
                .note("[ĐÃ THANH TOÁN]: Nhận 250,000 VNĐ")
                .build();

        when(orderRepository.existsByTransactionId("TX-555")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("HB20261013A1B2C3")).thenReturn(Optional.of(order));

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Ghi thêm note CHUYỂN TRÙNG và lưu vào DB
        assertTrue(result);
        assertTrue(order.getNote().contains("[CHUYỂN TRÙNG - CẦN HOÀN]"));
        assertTrue(order.getNote().contains("TX-555"));
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void processSepayWebhook_WhenUnderpaid_RecordsTxIdAndReturnsTrueToStopRetries() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan HB20261013A1B2C3")
                .referenceCode("TX-777")
                .transferAmount(BigDecimal.valueOf(100000))
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261013A1B2C3")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-777")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("HB20261013A1B2C3")).thenReturn(Optional.of(order));

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Returns true so SePay stops retrying, but order is marked as UNDERPAID
        assertTrue(result);
        assertEquals("UNDERPAID", order.getWebhookStatus());
        assertEquals("TX-777", order.getTransactionId());
        assertTrue(order.getNote().contains("[CẢNH BÁO]: Khách chuyển thiếu tiền"));
        verify(orderRepository).saveAndFlush(order);
    }

    @Test
    void processSepayWebhook_WhenConcurrentDuplicateViolatesUniqueConstraint_HandlesIdempotently() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan HB20261013A1B2C3")
                .referenceCode("TX-666")
                .transferAmount(BigDecimal.valueOf(250000))
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261013A1B2C3")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-666")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("HB20261013A1B2C3")).thenReturn(Optional.of(order));
        doThrow(new DataIntegrityViolationException("Duplicate entry 'TX-666' for key uk_orders_transaction"))
                .when(orderRepository).saveAndFlush(any());

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Catches constraint violation and returns true cleanly
        assertTrue(result);
    }

    @Test
    void processSepayWebhook_WhenUnexpectedExceptionOccurs_ReleasesRedisLockAndRethrows() {
        // Given
        org.springframework.data.redis.core.StringRedisTemplate mockRedis = mock(org.springframework.data.redis.core.StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        org.springframework.data.redis.core.ValueOperations<String, String> mockOps = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(mockRedis.opsForValue()).thenReturn(mockOps);
        when(mockOps.setIfAbsent(eq("lock:sepay:TX-ERR-1"), eq("LOCKED"), any(java.time.Duration.class))).thenReturn(true);

        org.springframework.test.util.ReflectionTestUtils.setField(paymentService, "redisTemplate", mockRedis);

        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan HB20261013A1B2C3")
                .referenceCode("TX-ERR-1")
                .transferAmount(BigDecimal.valueOf(250000))
                .build();

        when(orderRepository.existsByTransactionId("TX-ERR-1"))
                .thenThrow(new RuntimeException("Database connection blip"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                paymentService.processSepayWebhook(request, "Apikey secure_secret_123")
        );

        // Verify Redis lock was released in catch block
        verify(mockRedis, times(1)).delete("lock:sepay:TX-ERR-1");
    }
}
