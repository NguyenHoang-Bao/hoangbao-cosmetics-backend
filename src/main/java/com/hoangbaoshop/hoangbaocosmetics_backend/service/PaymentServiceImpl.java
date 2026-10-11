package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.config.PaymentProperties;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.payment.SepayWebhookRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentQrResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.payment.PaymentStatusResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;

    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("ORD-[A-Za-z0-9]+", Pattern.CASE_INSENSITIVE);

    // tạo mã qr chuyển khoản ngân hàng tự động (VietQR QuickLink)
    @Override
    @Transactional(readOnly = true)
    public PaymentQrResponse generateVietQr(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy, không thể tiến hành thanh toán!");
        }

        PaymentProperties.VietQr vietQrConfig = paymentProperties.getVietqr();
        long amount = order.getTotalAmount() != null ? order.getTotalAmount().longValue() : 0L;
        String encodedContent = URLEncoder.encode(order.getOrderCode(), StandardCharsets.UTF_8);
        String encodedAccountName = URLEncoder.encode(vietQrConfig.getAccountName(), StandardCharsets.UTF_8);

        // Chuẩn URL VietQR QuickLink (Napas247)
        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                vietQrConfig.getBankId(),
                vietQrConfig.getAccountNumber(),
                vietQrConfig.getTemplate(),
                amount,
                encodedContent,
                encodedAccountName
        );

        return PaymentQrResponse.builder()
                .orderCode(order.getOrderCode())
                .totalAmount(order.getTotalAmount())
                .qrCodeUrl(qrUrl)
                .bankId(vietQrConfig.getBankId())
                .accountNumber(vietQrConfig.getAccountNumber())
                .accountName(vietQrConfig.getAccountName())
                .content(order.getOrderCode())
                .paymentMethod("BANK_TRANSFER")
                .build();
    }

    // kiểm tra trạng thái thanh toán của đơn hàng (cho frontend polling)
    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse checkPaymentStatus(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        boolean isPaid = (order.getStatus() == OrderStatus.PROCESSING ||
                order.getStatus() == OrderStatus.SHIPPING ||
                order.getStatus() == OrderStatus.DELIVERED);

        return PaymentStatusResponse.builder()
                .orderCode(order.getOrderCode())
                .isPaid(isPaid)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .transactionId(order.getTransactionId())
                .paymentMethod(order.getPaymentMethod())
                .build();
    }

    // xử lý webhook sepay tự động xác nhận chuyển khoản ngân hàng
    @Override
    @Transactional
    public boolean processSepayWebhook(SepayWebhookRequest request, String authorizationHeader) {
        // 1. Kiểm tra API Key xác thực từ SePay (Fail-closed: bắt buộc phải có key hợp lệ)
        String configuredApiKey = paymentProperties.getSepay() != null ? paymentProperties.getSepay().getApiKey() : null;
        if (configuredApiKey == null || configuredApiKey.isBlank() || "SEPAY_API_KEY_SECRET".equals(configuredApiKey)) {
            log.error("SePay Webhook bị từ chối: SEPAY_API_KEY chưa được cấu hình an toàn trên hệ thống (Fail-closed)");
            return false;
        }

        String expectedHeader = "Apikey " + configuredApiKey.trim();
        if (authorizationHeader == null || !MessageDigest.isEqual(
                expectedHeader.getBytes(StandardCharsets.UTF_8),
                authorizationHeader.trim().getBytes(StandardCharsets.UTF_8))) {
            log.warn("SePay Webhook bị từ chối: Authorization header không hợp lệ");
            return false;
        }

        // 2. Chỉ xử lý giao dịch tiền vào tài khoản ("in")
        if (request.getTransferType() != null && !"in".equalsIgnoreCase(request.getTransferType())) {
            log.info("SePay Webhook: Bỏ qua giao dịch tiền ra (transferType = {})", request.getTransferType());
            return true;
        }

        // 3. Trích xuất mã đơn hàng từ nội dung chuyển khoản
        String content = request.getContent();
        if (content == null || content.isBlank()) {
            log.warn("SePay Webhook: Nội dung giao dịch trống");
            return false;
        }

        Matcher matcher = ORDER_CODE_PATTERN.matcher(content);
        String orderCode = null;
        if (matcher.find()) {
            orderCode = matcher.group().toUpperCase();
        }

        if (orderCode == null) {
            log.info("SePay Webhook: Không tìm thấy mã đơn dạng ORD-XXXXXXXX trong nội dung: '{}'", content);
            return false;
        }

        // 4. Trích xuất mã giao dịch duy nhất từ SePay
        String txId = request.getReferenceCode() != null && !request.getReferenceCode().isBlank()
                ? request.getReferenceCode().trim()
                : String.valueOf(request.getId());

        // 5. Kiểm tra Idempotency: Nếu mã giao dịch (txId) này đã được xử lý trước đó -> Bỏ qua ngay
        if (orderRepository.existsByTransactionId(txId)) {
            log.info("SePay Webhook (Idempotent): Giao dịch '{}' đã được xử lý trước đó. Xác nhận thành công để tránh SePay retry.", txId);
            return true;
        }

        // 6. Tìm đơn hàng tương ứng với khóa bi quan (Pessimistic Write Lock để chống Race Condition)
        Optional<Order> orderOpt = orderRepository.findByOrderCodeForUpdate(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("SePay Webhook: Không tìm thấy đơn hàng trong hệ thống với mã: {}", orderCode);
            return false;
        }

        Order order = orderOpt.get();
        BigDecimal transferAmount = request.getTransferAmount() != null ? request.getTransferAmount() : BigDecimal.ZERO;

        // 7. Xử lý tình huống đặc biệt: Đơn hàng đã bị HỦY trước đó nhưng khách vẫn chuyển tiền
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("SePay Webhook: Đơn hàng {} đã bị HỦY trước đó nhưng nhận được thanh toán: {} VNĐ (Mã GD: {}). Cần liên hệ hoàn tiền!",
                    orderCode, String.format("%,.0f", transferAmount.doubleValue()), txId);
            order.setWebhookStatus("PAID_BUT_CANCELLED");
            order.setTransactionId(txId);
            String refundNote = String.format("[CẢNH BÁO HOÀN TIỀN]: Khách chuyển %,.0f VNĐ vào đơn đã hủy trước đó (Mã GD: %s)", transferAmount.doubleValue(), txId);
            String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(currentNote + refundNote);
            try {
                orderRepository.saveAndFlush(order);
            } catch (DataIntegrityViolationException dup) {
                log.info("SePay Webhook (Idempotent): Giao dịch {} đã được ghi nhận bởi tiến trình khác.", txId);
            }
            return true;
        }

        // 8. Nếu đơn đã ở trạng thái thanh toán rồi -> Bỏ qua
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("SePay Webhook: Đơn hàng {} đã ở trạng thái {} trước đó. Bỏ qua.", orderCode, order.getStatus());
            return true;
        }

        // 9. Kiểm tra số tiền chuyển có đủ không
        if (transferAmount.compareTo(order.getTotalAmount()) < 0) {
            log.warn("SePay Webhook: Số tiền chuyển ({} VNĐ) không đủ cho đơn hàng {} (yêu cầu {} VNĐ)",
                    String.format("%,.0f", transferAmount.doubleValue()), orderCode, String.format("%,.0f", order.getTotalAmount().doubleValue()));
            order.setWebhookStatus("UNDERPAID");
            order.setTransactionId(txId);
            String underpaidNote = String.format("[CẢNH BÁO]: Khách chuyển thiếu tiền. Nhận: %,.0f VNĐ, Cần: %,.0f VNĐ (Mã GD: %s)",
                    transferAmount.doubleValue(), order.getTotalAmount().doubleValue(), txId);
            String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(currentNote + underpaidNote);
            try {
                orderRepository.saveAndFlush(order);
            } catch (DataIntegrityViolationException dup) {
                log.info("SePay Webhook (Idempotent): Giao dịch {} đã được ghi nhận bởi tiến trình khác.", txId);
            }
            return true;
        }

        // 10. Cập nhật đơn hàng sang PROCESSING và ghi nhận giao dịch thành công
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setTransactionId(txId);
        order.setWebhookStatus("SUCCESS");

        String gatewayName = request.getGateway() != null ? request.getGateway() : "Ngân hàng";
        String successNote = String.format("[ĐÃ THANH TOÁN]: Nhận %,.0f VNĐ qua %s, Mã GD: %s",
                transferAmount.doubleValue(), gatewayName, txId);
        String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
        order.setNote(currentNote + successNote);

        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException dup) {
            log.info("SePay Webhook (Idempotent): Giao dịch {} đã được ghi nhận bởi tiến trình khác.", txId);
            return true;
        }

        log.info("SePay Webhook: Xác nhận thanh toán thành công cho đơn hàng {} với số tiền {} VNĐ (Mã GD: {})!",
                orderCode, String.format("%,.0f", transferAmount), txId);
        return true;
    }
}
