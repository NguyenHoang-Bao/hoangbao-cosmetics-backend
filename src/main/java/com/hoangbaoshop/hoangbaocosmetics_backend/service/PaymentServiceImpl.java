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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;

    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("ORD-[A-Za-z0-9]+", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional(readOnly = true)
    public PaymentQrResponse generateVietQr(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy, không thể tiến hành thanh toán!");
        }

        PaymentProperties.VietQr vietQrConfig = paymentProperties.getVietqr();
        long amount = Math.round(order.getTotalAmount());
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

    @Override
    @Transactional
    public boolean processSepayWebhook(SepayWebhookRequest request, String authorizationHeader) {
        // 1. Kiểm tra API Key xác thực từ SePay (nếu đã cấu hình)
        String configuredApiKey = paymentProperties.getSepay().getApiKey();
        if (configuredApiKey != null && !configuredApiKey.isBlank() && !"SEPAY_API_KEY_SECRET".equals(configuredApiKey)) {
            if (authorizationHeader == null || !authorizationHeader.contains(configuredApiKey)) {
                log.warn("SePay Webhook bị từ chối: Authorization header không hợp lệ: {}", authorizationHeader);
                return false;
            }
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

        // 4. Tìm đơn hàng tương ứng
        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("SePay Webhook: Không tìm thấy đơn hàng trong hệ thống với mã: {}", orderCode);
            return false;
        }

        Order order = orderOpt.get();

        // 5. Nếu đơn đã thanh toán hoặc đã xử lý rồi thì không xử lý lại
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("SePay Webhook: Đơn hàng {} đã được xử lý trước đó (trạng thái hiện tại: {})", orderCode, order.getStatus());
            return true;
        }

        // 6. Kiểm tra số tiền chuyển có đủ không
        Double transferAmount = request.getTransferAmount();
        if (transferAmount == null || transferAmount < order.getTotalAmount()) {
            log.warn("SePay Webhook: Số tiền chuyển ({} VNĐ) không đủ cho đơn hàng {} (yêu cầu {} VNĐ)",
                    transferAmount, orderCode, order.getTotalAmount());
            order.setWebhookStatus("UNDERPAID");
            String underpaidNote = String.format("[CẢNH BÁO]: Khách chuyển thiếu tiền. Nhận: %,.0f VNĐ, Cần: %,.0f VNĐ",
                    transferAmount != null ? transferAmount : 0.0, order.getTotalAmount());
            String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(currentNote + underpaidNote);
            orderRepository.save(order);
            return false;
        }

        // 7. Cập nhật đơn hàng sang PROCESSING và ghi nhận giao dịch
        order.setStatus(OrderStatus.PROCESSING);
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        String txId = request.getReferenceCode() != null && !request.getReferenceCode().isBlank()
                ? request.getReferenceCode()
                : String.valueOf(request.getId());
        order.setTransactionId(txId);
        order.setWebhookStatus("SUCCESS");

        String gatewayName = request.getGateway() != null ? request.getGateway() : "Ngân hàng";
        String successNote = String.format("[ĐÃ THANH TOÁN]: Nhận %,.0f VNĐ qua %s, Mã GD: %s",
                transferAmount, gatewayName, txId);
        String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
        order.setNote(currentNote + successNote);

        orderRepository.save(order);
        log.info("SePay Webhook: Đã xác nhận thanh toán thành công cho đơn hàng {} với số tiền %,.0f VNĐ!", orderCode, transferAmount);
        return true;
    }
}
