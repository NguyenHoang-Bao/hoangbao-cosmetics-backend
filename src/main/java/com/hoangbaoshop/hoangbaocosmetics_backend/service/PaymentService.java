package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentQrResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentStatusResponse;

public interface PaymentService {

    // tạo mã qr chuyển khoản ngân hàng tự động (VietQR / SePay)
    PaymentQrResponse generateVietQr(String orderCode);

    // tạo mã qr kèm kiểm tra số điện thoại người nhận
    PaymentQrResponse generateVietQr(String orderCode, String phone);

    // kiểm tra đơn hàng đã thanh toán thành công chưa
    PaymentStatusResponse checkPaymentStatus(String orderCode);

    // kiểm tra trạng thái đơn hàng kèm đối soát số điện thoại
    PaymentStatusResponse checkPaymentStatus(String orderCode, String phone);

    // xử lý webhook khi ngân hàng báo tiền về (xác thực token & chống trùng lặp idempotency)
    boolean processSepayWebhook(SepayWebhookRequest request, String authorizationHeader);
}
