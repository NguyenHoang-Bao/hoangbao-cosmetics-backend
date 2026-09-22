package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentQrResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentStatusResponse;

public interface PaymentService {

    PaymentQrResponse generateVietQr(String orderCode);

    PaymentStatusResponse checkPaymentStatus(String orderCode);

    boolean processSepayWebhook(SepayWebhookRequest request, String authorizationHeader);
}
