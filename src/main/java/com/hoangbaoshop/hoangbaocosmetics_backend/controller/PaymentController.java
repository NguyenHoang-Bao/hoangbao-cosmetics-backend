package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentQrResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentStatusResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;

    // 1. Sinh mã VietQR cho đơn hàng (Khách xem mã QR để chuyển khoản)
    @GetMapping("/vietqr/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentQrResponse>> getVietQr(@PathVariable String orderCode) {
        PaymentQrResponse response = paymentService.generateVietQr(orderCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin mã VietQR thành công!"));
    }

    // 2. Kiểm tra trạng thái thanh toán (Frontend polling 2s/lần để biết khi nào khách đã chuyển tiền)
    @GetMapping("/check-status/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> checkStatus(@PathVariable String orderCode) {
        PaymentStatusResponse response = paymentService.checkPaymentStatus(orderCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Webhook nhận biến động số dư từ SePay
    @PostMapping("/sepay-webhook")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestBody SepayWebhookRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        boolean processed = paymentService.processSepayWebhook(request, authHeader);
        if (processed) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Webhook processed successfully"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Webhook processing skipped or failed"
            ));
        }
    }
}
