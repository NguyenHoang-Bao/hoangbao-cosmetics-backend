package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentQrResponse {

    private String orderCode;
    private Double totalAmount;
    private String qrCodeUrl;
    private String bankId;
    private String accountNumber;
    private String accountName;
    private String content; // Nội dung chuyển khoản
    private String paymentMethod;
}
