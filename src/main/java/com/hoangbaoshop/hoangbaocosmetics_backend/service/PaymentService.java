package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentQrResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentStatusResponse;

public interface PaymentService {

    // tạo mã qr chuyển khoản ngân hàng tự động (VietQR / SePay)
    PaymentQrResponse generateVietQr(String orderCode);

    // kiểm tra đơn hàng đã thanh toán thành công chưa
    PaymentStatusResponse checkPaymentStatus(String orderCode);

    // xử lý webhook khi ngân hàng báo tiền về (xác thực token & chống trùng lặp idempotency)
    boolean processSepayWebhook(SepayWebhookRequest request, String authorizationHeader);
}
