package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Serialization phải trả về đúng trường 'isPaid' để tương thích với Frontend Web và POS Desktop")
    void testSerializationIncludesIsPaidField() throws Exception {
        PaymentStatusResponse response = PaymentStatusResponse.builder()
                .orderCode("HB20261027TEST")
                .isPaid(true)
                .status(OrderStatus.PROCESSING)
                .totalAmount(new BigDecimal("500000"))
                .transactionId("FT-123456")
                .paymentMethod(PaymentMethod.VIETQR)
                .build();

        String json = objectMapper.writeValueAsString(response);

        // Đảm bảo JSON chứa "isPaid":true chứ không phải "paid":true
        assertThat(json).contains("\"isPaid\":true");
        assertThat(json).doesNotContain("\"paid\":true");
    }

    @Test
    @DisplayName("Deserialization phải parse được đúng trường 'isPaid'")
    void testDeserializationIncludesIsPaidField() throws Exception {
        String json = "{\"orderCode\":\"HB20261027TEST\",\"isPaid\":true,\"status\":\"PROCESSING\",\"totalAmount\":500000,\"transactionId\":\"FT-123456\",\"paymentMethod\":\"VIETQR\"}";

        PaymentStatusResponse response = objectMapper.readValue(json, PaymentStatusResponse.class);

        assertThat(response.isPaid()).isTrue();
        assertThat(response.getOrderCode()).isEqualTo("HB20261027TEST");
    }
}
