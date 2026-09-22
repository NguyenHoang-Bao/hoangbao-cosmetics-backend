package com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayWebhookRequest {

    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType; // "in" là nhận tiền, "out" là chuyển đi
    private Double transferAmount;
    private Double accumulated;
    private String subAccount;
    private String referenceCode;
    private String description;
}
