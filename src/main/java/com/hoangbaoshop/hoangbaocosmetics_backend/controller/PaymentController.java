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
public class PaymentController {

    private final PaymentService paymentService;

    // [GET] sinh mã VietQR cho đơn hàng (chuyển khoản ngân hàng)
    @GetMapping("/vietqr/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentQrResponse>> getVietQr(
            @PathVariable String orderCode,
            @RequestParam(value = "phone", required = false) String phone
    ) {
        PaymentQrResponse response = paymentService.generateVietQr(orderCode, phone);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin mã VietQR thành công!"));
    }

    // [GET] kiểm tra trạng thái thanh toán (polling)
    @GetMapping("/check-status/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> checkStatus(
            @PathVariable String orderCode,
            @RequestParam(value = "phone", required = false) String phone
    ) {
        PaymentStatusResponse response = paymentService.checkPaymentStatus(orderCode, phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [POST] webhook nhận thông báo biến động số dư từ SePay
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Webhook processing rejected: Unauthorized"
            ));
        }
    }
}
