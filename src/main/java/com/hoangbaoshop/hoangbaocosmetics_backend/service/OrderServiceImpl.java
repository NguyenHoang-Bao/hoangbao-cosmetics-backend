package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderDetailResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.InventoryLog;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.TransactionType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.InventoryLogRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.email.OrderEmailDto;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // tạo đơn hàng online (tính tiền, trừ tồn kho, ghi log xuất bán)
    @Override
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        String orderCode = generateOrderCode();

        User user = null;
        if (username != null && !username.isBlank()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        BigDecimal shippingFee = BigDecimal.valueOf(30000);

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .orderType(OrderType.ONLINE)
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .deliveryAddress(request.getDeliveryAddress())
                .shippingFee(shippingFee)
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .orderDetails(new ArrayList<>())
                .build();

        BigDecimal totalProductPrice = buildOrderLines(order, request.getItems());
        order.setTotalProductPrice(totalProductPrice);
        order.setTotalAmount(totalProductPrice.add(shippingFee));

        Order savedOrder = orderRepository.save(order);
        writeSaleLedger(savedOrder, user, "Xuất bán đơn hàng Online #");

        // Gửi email xác nhận đơn hàng bất đồng bộ tới khách hàng sau khi transaction commit thành công
        String recipientEmail = (user != null && user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : request.getEmail();
        if (recipientEmail != null && !recipientEmail.isBlank()) {
            OrderEmailDto emailDto = mapToOrderEmailDto(savedOrder);
            executeAfterCommit(() -> emailService.sendOrderConfirmationEmail(emailDto, recipientEmail.trim()));
        }

        return mapToOrderResponse(savedOrder);
    }

    // lấy danh sách đơn hàng của người dùng hiện tại
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String username, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserUsername(username, pageable);
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNo(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    // tra cứu đơn hàng theo mã đơn (kiểm tra quyền hoặc sđt khách)
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode, String phoneNumber, String currentUsername) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // 1. Kiểm tra nếu người dùng đã đăng nhập và là chủ sở hữu hoặc nhân viên/admin
        boolean isAuthorized = false;
        if (currentUsername != null && !currentUsername.isBlank()) {
            if (order.getUser() != null && currentUsername.equalsIgnoreCase(order.getUser().getUsername())) {
                isAuthorized = true;
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
                    boolean isAdminOrStaff = auth.getAuthorities().stream()
                            .anyMatch(a -> {
                                String authName = a.getAuthority();
                                return "ROLE_ADMIN".equals(authName) || "ADMIN".equals(authName)
                                        || "ROLE_STAFF".equals(authName) || "STAFF".equals(authName)
                                        || "ROLE_ONLINE_STAFF".equals(authName) || "ONLINE_STAFF".equals(authName)
                                        || "ROLE_WAREHOUSE_MANAGER".equals(authName) || "WAREHOUSE_MANAGER".equals(authName);
                            });
                    if (isAdminOrStaff) {
                        isAuthorized = true;
                    }
                }
            }
        }

        // 2. Nếu không phải chủ tài khoản hay nhân viên (khách vãng lai tra cứu)
        // -> Bắt buộc phải cung cấp số điện thoại đặt hàng để xác thực (Bịt lỗ hổng IDOR)
        if (!isAuthorized) {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new BadRequestException("Vui lòng cung cấp số điện thoại đặt hàng để xác thực tra cứu đơn hàng!");
            }

            // Chuẩn hóa loại bỏ các ký tự đặc biệt, chỉ giữ số để so sánh chính xác
            String cleanInputPhone = phoneNumber.replaceAll("[^0-9]", "");
            String cleanOrderPhone = order.getPhoneNumber() != null ? order.getPhoneNumber().replaceAll("[^0-9]", "") : "";

            if (!cleanOrderPhone.equals(cleanInputPhone)) {
                throw new BadRequestException("Số điện thoại không khớp với thông tin đơn hàng!");
            }
        }

        return mapToOrderResponse(order);
    }

    // ==================== ADMIN & STAFF METHODS ====================

    // lấy danh sách đơn hàng cho admin/staff kèm bộ lọc và phân trang
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrdersAdmin(
            OrderStatus status,
            OrderType orderType,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Order> page = orderRepository.searchOrdersAdmin(status, orderType, trimmedKeyword, startDate, endDate, pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // lấy chi tiết đơn hàng by id cho admin/staff
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailAdmin(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponse(order);
    }

    // cập nhật trạng thái đơn hàng (kiểm tra luồng hợp lệ, hoàn kho nếu hủy)
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, UpdateOrderStatusRequest request, String staffUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new BadRequestException("Đơn hàng đã giao thành công, không thể thay đổi trạng thái!");
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy, không thể thay đổi trạng thái!");
        }
        if (currentStatus == newStatus) {
            return mapToOrderResponse(order);
        }

        // Kiểm tra hợp lệ của luồng chuyển đổi trạng thái
        validateStatusTransition(currentStatus, newStatus);

        // Nếu chuyển sang trạng thái CANCELLED -> Hoàn lại tồn kho và ghi sổ cái
        if (newStatus == OrderStatus.CANCELLED) {
            User staffUser = (staffUsername != null && !staffUsername.isBlank()) ? userRepository.findByUsername(staffUsername).orElse(null) : null;
            rollbackStockForOrder(order, staffUser, request.getNote());
        }

        order.setStatus(newStatus);
        if (request.getNote() != null && !request.getNote().isBlank()) {
            String existingNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(existingNote + request.getNote().trim());
        }

        if (staffUsername != null && !staffUsername.isBlank()) {
            User staff = userRepository.findByUsername(staffUsername).orElse(null);
            order.setStaff(staff);
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    // hủy đơn hàng từ phía admin/staff (hoàn lại kho và ghi sổ cái)
    @Override
    @Transactional
    public OrderResponse cancelOrderAdmin(Integer orderId, CancelOrderRequest request, String staffUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Đơn hàng đã giao thành công, không thể hủy!");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng này đã bị hủy trước đó!");
        }

        User staffUser = (staffUsername != null && !staffUsername.isBlank()) ? userRepository.findByUsername(staffUsername).orElse(null) : null;
        // Hoàn lại tồn kho cho tất cả các sản phẩm trong đơn và ghi sổ cái
        rollbackStockForOrder(order, staffUser, request.getReason());

        order.setStatus(OrderStatus.CANCELLED);
        String cancelNote = "[HỦY ĐƠN]: " + request.getReason().trim();
        String existingNote = order.getNote() != null ? order.getNote() + " | " : "";
        order.setNote(existingNote + cancelNote);

        if (staffUsername != null && !staffUsername.isBlank()) {
            User staff = userRepository.findByUsername(staffUsername).orElse(null);
            order.setStaff(staff);
        }

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    // tạo đơn hàng bán tại quầy POS (trừ kho và hoàn thành ngay)
    @Override
    @Transactional
    public OrderResponse createPosOrder(CreatePosOrderRequest request, String staffUsername) {
        String orderCode = generateOrderCode();

        User staff = null;
        if (staffUsername != null && !staffUsername.isBlank()) {
            staff = userRepository.findByUsername(staffUsername).orElse(null);
        }

        BigDecimal shippingFee = BigDecimal.ZERO; // Bán tại quầy không tính phí vận chuyển

        String customerName = (request.getCustomerName() != null && !request.getCustomerName().isBlank())
                ? request.getCustomerName().trim()
                : "Khách mua tại quầy";

        Order order = Order.builder()
                .orderCode(orderCode)
                .staff(staff)
                .orderType(OrderType.OFFLINE)
                .customerName(customerName)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : "TẠI QUẦY")
                .deliveryAddress("Mua tại cửa hàng")
                .shippingFee(shippingFee)
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.DELIVERED) // Khách mua tại quầy nhận hàng ngay
                .note(request.getNote())
                .orderDetails(new ArrayList<>())
                .build();

        BigDecimal totalProductPrice = buildOrderLines(order, request.getItems());
        order.setTotalProductPrice(totalProductPrice);
        order.setTotalAmount(totalProductPrice.add(shippingFee));

        Order savedOrder = orderRepository.save(order);
        writeSaleLedger(savedOrder, staff, "Xuất bán tại quầy POS đơn #");

        return mapToOrderResponse(savedOrder);
    }

    // quét và tự động hủy các đơn chuyển khoản ngân hàng quá hạn thanh toán 30 phút, hoàn tồn kho
    @Override
    @Transactional
    public int cancelExpiredBankTransferOrders() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod> transferMethods = List.of(
                com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod.BANK_TRANSFER,
                com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod.VIETQR
        );

        List<Order> expiredOrders = orderRepository.findByStatusAndPaymentMethodInAndCreatedAtBefore(
                OrderStatus.PENDING, transferMethods, thirtyMinutesAgo);

        for (Order order : expiredOrders) {
            rollbackStockForOrder(order, null, "Tự hủy: quá 30 phút chưa thanh toán");
            order.setStatus(OrderStatus.CANCELLED);
            String cancelNote = "[HỆ THỐNG]: Tự động hủy do quá 30 phút chưa thanh toán";
            String currentNote = order.getNote() != null ? order.getNote() + " | " : "";
            order.setNote(currentNote + cancelNote);
        }

        if (!expiredOrders.isEmpty()) {
            orderRepository.saveAll(expiredOrders);
        }
        return expiredOrders.size();
    }

    // ==================== HELPER METHODS ====================

    // kiểm tra hợp lệ khi chuyển đổi trạng thái đơn hàng
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case PENDING:
                if (next != OrderStatus.PROCESSING && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'PENDING', chỉ có thể chuyển sang 'PROCESSING' hoặc 'CANCELLED'!");
                }
                break;
            case PROCESSING:
                if (next != OrderStatus.SHIPPING && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'PROCESSING', chỉ có thể chuyển sang 'SHIPPING' hoặc 'CANCELLED'!");
                }
                break;
            case SHIPPING:
                if (next != OrderStatus.DELIVERED && next != OrderStatus.CANCELLED) {
                    throw new BadRequestException("Đơn hàng đang ở trạng thái 'SHIPPING', chỉ có thể chuyển sang 'DELIVERED' hoặc 'CANCELLED' (hàng trả lại/giao thất bại)!");
                }
                break;
            default:
                throw new BadRequestException("Không thể chuyển đổi từ trạng thái " + current + " sang " + next);
        }
    }

    // hoàn lại tồn kho và ghi sổ cái nhập trả khi hủy đơn
    private void rollbackStockForOrder(Order order, User actor, String reason) {
        if (order.getOrderDetails() != null) {
            User logActor = actor != null ? actor : userRepository.findByUsername("admin").orElse(null);
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = detail.getVariant();
                if (variant != null) {
                    productVariantRepository.increaseStockQuantity(variant.getIdVariant(), detail.getQuantity());

                    if (logActor != null) {
                        String logNote = "[HỦY ĐƠN #" + order.getOrderCode() + "]: " + ((reason != null && !reason.isBlank()) ? reason.trim() : "Hoàn lại kho");
                        InventoryLog log = InventoryLog.builder()
                                .variant(variant)
                                .user(logActor)
                                .transactionType(TransactionType.RETURN)
                                .quantityChange(detail.getQuantity())
                                .referenceId(order.getIdOrder())
                                .note(logNote)
                                .build();
                        inventoryLogRepository.save(log);
                    }
                }
            }
        }
    }

    // xử lý chi tiết đơn hàng, trừ tồn kho nguyên tử và tính tổng tiền
    private BigDecimal buildOrderLines(Order order, List<OrderItemRequest> items) {
        // 1. Sắp xếp items theo idVariant để ngăn ngừa Deadlock giữa các transaction mua đồng thời
        List<OrderItemRequest> sortedItems = items.stream()
                .sorted(Comparator.comparing(OrderItemRequest::getIdVariant))
                .toList();

        List<Integer> variantIds = sortedItems.stream()
                .map(OrderItemRequest::getIdVariant)
                .toList();

        // 2. Batch load toàn bộ biến thể và eager load Product trong 1 câu SQL duy nhất
        Map<Integer, ProductVariant> variantMap = productVariantRepository.findAllWithProductByIdIn(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getIdVariant, v -> v));

        BigDecimal totalProductPrice = BigDecimal.ZERO;
        for (OrderItemRequest item : sortedItems) {
            ProductVariant variant = variantMap.get(item.getIdVariant());
            if (variant == null) {
                throw new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getIdVariant());
            }

            // Trừ tồn kho nguyên tử ở cấp Database (Chống âm kho khi nhiều người mua cùng lúc)
            int updatedRows = productVariantRepository.decreaseStockQuantity(variant.getIdVariant(), item.getQuantity());
            if (updatedRows == 0) {
                String productName = variant.getProduct() != null ? variant.getProduct().getName() : "Sản phẩm";
                throw new BadRequestException("Sản phẩm '" + productName + " (" + variant.getCapacity() + ")' không đủ số lượng tồn kho");
            }

            BigDecimal itemPrice = variant.getPrice();
            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalProductPrice = totalProductPrice.add(lineTotal);

            OrderDetail orderDetail = OrderDetail.builder()
                    .variant(variant)
                    .quantity(item.getQuantity())
                    .price(itemPrice)
                    .isReviewed(false)
                    .build();

            order.addOrderDetail(orderDetail);
        }
        return totalProductPrice;
    }

    // ghi sổ cái biến động kho khi xuất bán
    private void writeSaleLedger(Order order, User actor, String notePrefix) {
        User logActor = actor != null ? actor : userRepository.findByUsername("admin").orElse(null);
        if (logActor != null && order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<InventoryLog> logs = order.getOrderDetails().stream()
                    .map(detail -> InventoryLog.builder()
                            .variant(detail.getVariant())
                            .user(logActor)
                            .transactionType(TransactionType.EXPORT_SALE)
                            .quantityChange(-detail.getQuantity())
                            .referenceId(order.getIdOrder())
                            .note(notePrefix + order.getOrderCode())
                            .build())
                    .toList();
            inventoryLogRepository.saveAll(logs);
        }
    }

    // sinh mã đơn hàng tự động duy nhất
    private String generateOrderCode() {
        String datePart = LocalDate.now().format(ORDER_DATE_FORMATTER);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "HB" + datePart + randomPart;
    }

    // hàm chung map entity sang dto
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderDetailResponse> items = order.getOrderDetails().stream()
                .map(detail -> {
                    ProductVariant variant = detail.getVariant();
                    String productName = (variant != null && variant.getProduct() != null)
                            ? variant.getProduct().getName()
                            : null;
                    String capacity = variant != null ? variant.getCapacity() : null;
                    String sku = variant != null ? variant.getSku() : null;
                    BigDecimal subTotal = (detail.getPrice() != null && detail.getQuantity() != null)
                            ? detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()))
                            : BigDecimal.ZERO;

                    return OrderDetailResponse.builder()
                            .idOrderDetail(detail.getIdOrderDetail())
                            .idVariant(variant != null ? variant.getIdVariant() : null)
                            .productName(productName)
                            .capacity(capacity)
                            .sku(sku)
                            .quantity(detail.getQuantity())
                            .price(detail.getPrice())
                            .subTotal(subTotal)
                            .isReviewed(detail.getIsReviewed())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .idOrder(order.getIdOrder())
                .orderCode(order.getOrderCode())
                .idUser(order.getUser() != null ? order.getUser().getIdUser() : null)
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .orderType(order.getOrderType())
                .totalProductPrice(order.getTotalProductPrice())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .webhookStatus(order.getWebhookStatus())
                .status(order.getStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    // chuyển đổi sang snapshot DTO phục vụ gửi email phi tập trung an toàn với luồng async
    private OrderEmailDto mapToOrderEmailDto(Order order) {
        List<OrderEmailDto.Item> items = order.getOrderDetails() != null
                ? order.getOrderDetails().stream()
                .map(d -> {
                    ProductVariant v = d.getVariant();
                    String name = (v != null && v.getProduct() != null) ? v.getProduct().getName() : "Sản phẩm";
                    String cap = (v != null && v.getCapacity() != null) ? v.getCapacity() : "";
                    BigDecimal pr = d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO;
                    return OrderEmailDto.Item.builder()
                            .productName(name)
                            .capacity(cap)
                            .quantity(d.getQuantity())
                            .price(pr)
                            .lineTotal(pr.multiply(BigDecimal.valueOf(d.getQuantity())))
                            .build();
                })
                .toList()
                : Collections.emptyList();

        return OrderEmailDto.builder()
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
                .createdAt(order.getCreatedAt())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }

    // thực thi tác vụ sau khi database transaction đã commit thành công
    private void executeAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
