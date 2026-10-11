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
                .content("Thanh toan don ORD-12345678")
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
                .content("Thanh toan ORD-12345678")
                .referenceCode("TX-888")
                .transferAmount(BigDecimal.valueOf(250000))
                .gateway("MBBank")
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("ORD-12345678")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-888")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("ORD-12345678")).thenReturn(Optional.of(order));

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
    void processSepayWebhook_WhenUnderpaid_RecordsTxIdAndReturnsTrueToStopRetries() {
        // Given
        sepayConfig.setApiKey("secure_secret_123");
        SepayWebhookRequest request = SepayWebhookRequest.builder()
                .transferType("in")
                .content("Thanh toan ORD-12345678")
                .referenceCode("TX-777")
                .transferAmount(BigDecimal.valueOf(100000))
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("ORD-12345678")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-777")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("ORD-12345678")).thenReturn(Optional.of(order));

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
                .content("Thanh toan ORD-12345678")
                .referenceCode("TX-666")
                .transferAmount(BigDecimal.valueOf(250000))
                .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("ORD-12345678")
                .totalAmount(BigDecimal.valueOf(250000))
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.existsByTransactionId("TX-666")).thenReturn(false);
        when(orderRepository.findByOrderCodeForUpdate("ORD-12345678")).thenReturn(Optional.of(order));
        doThrow(new DataIntegrityViolationException("Duplicate entry 'TX-666' for key uk_orders_transaction"))
                .when(orderRepository).saveAndFlush(any());

        // When
        boolean result = paymentService.processSepayWebhook(request, "Apikey secure_secret_123");

        // Then: Catches constraint violation and returns true cleanly
        assertTrue(result);
    }
}
