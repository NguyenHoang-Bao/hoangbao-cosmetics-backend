package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CreateOrderRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.OrderItemRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.order.OrderResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Order;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderType;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.PaymentMethod;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.InventoryLogRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductVariantRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryLogRepository inventoryLogRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetOrderByCode_AdminAuthorityFromSecurityContext_DoesNotQueryUserRepository() {
        // Given
        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HBC-123456")
                .phoneNumber("0912345678")
                .status(OrderStatus.PENDING)
                .orderType(OrderType.ONLINE)
                .paymentMethod(PaymentMethod.COD)
                .user(User.builder().idUser(10).username("customer1").build())
                .build();

        when(orderRepository.findByOrderCode("HBC-123456")).thenReturn(Optional.of(order));

        // Authenticate admin in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin_user",
                        "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        // When
        OrderResponse response = orderService.getOrderByCode("HBC-123456", null, "admin_user");

        // Then
        assertNotNull(response);
        assertEquals("HBC-123456", response.getOrderCode());

        // Verify userRepository.findByUsername was NEVER called because authorities were in SecurityContext
        verify(userRepository, never()).findByUsername("admin_user");
    }

    @Test
    void testCreateOrder_ProcessesVariantsInSortedOrder_ToPreventDeadlock() {
        // Given
        OrderItemRequest item1 = OrderItemRequest.builder().idVariant(50).quantity(2).build();
        OrderItemRequest item2 = OrderItemRequest.builder().idVariant(20).quantity(1).build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Khách hàng")
                .phoneNumber("0987654321")
                .deliveryAddress("Hà Nội")
                .paymentMethod(PaymentMethod.COD)
                .items(Arrays.asList(item1, item2)) // Unsorted (50, 20)
                .build();

        User customer = User.builder().idUser(5).username("customer_user").build();
        when(userRepository.findByUsername("customer_user")).thenReturn(Optional.of(customer));

        Product product = Product.builder().idProduct(1).name("Son môi").build();
        ProductVariant variant20 = ProductVariant.builder()
                .idVariant(20)
                .product(product)
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(10)
                .build();
        ProductVariant variant50 = ProductVariant.builder()
                .idVariant(50)
                .product(product)
                .price(BigDecimal.valueOf(200000))
                .stockQuantity(10)
                .build();

        when(productVariantRepository.findAllWithProductByIdIn(anyCollection()))
                .thenReturn(Arrays.asList(variant20, variant50));
        when(productVariantRepository.decreaseStockQuantity(20, 1)).thenReturn(1);
        when(productVariantRepository.decreaseStockQuantity(50, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.createOrder("customer_user", request);

        // Then: verify lock/decreaseStock calls were executed strictly in ascending ID order (20 then 50)
        InOrder inOrder = inOrder(productVariantRepository);
        inOrder.verify(productVariantRepository).decreaseStockQuantity(20, 1);
        inOrder.verify(productVariantRepository).decreaseStockQuantity(50, 2);

        // Verify single batch load query and batch ledger save
        verify(productVariantRepository, times(1)).findAllWithProductByIdIn(anyCollection());
        verify(inventoryLogRepository, times(1)).saveAll(anyList());

        // Verify: order code matches date-based format HB + yyyyMMdd + XXXXXX and requires NO findByOrderCode query
        assertNotNull(response);
        assertNotNull(response.getOrderCode());
        org.junit.jupiter.api.Assertions.assertTrue(
                response.getOrderCode().matches("^HB\\d{8}[A-Z0-9]{6}$"),
                "Mã đơn hàng phải tuân theo định dạng chuẩn HB-YYYYMMDD-XXXXXX không có ký tự đặc biệt"
        );
        verify(orderRepository, never()).findByOrderCode(anyString());
    }

    @Test
    void testCreateOrder_WithEmail_SendsOrderConfirmationEmailWithDto() {
        OrderItemRequest item = OrderItemRequest.builder().idVariant(10).quantity(1).build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Khách Mua Hàng")
                .phoneNumber("0912345678")
                .email("buyer@gmail.com")
                .deliveryAddress("TP. Hồ Chí Minh")
                .paymentMethod(PaymentMethod.COD)
                .items(Collections.singletonList(item))
                .build();

        Product product = Product.builder().idProduct(1).name("Kem Dưỡng").build();
        ProductVariant variant10 = ProductVariant.builder()
                .idVariant(10)
                .product(product)
                .capacity("50ml")
                .price(BigDecimal.valueOf(250000))
                .stockQuantity(10)
                .build();

        when(productVariantRepository.findAllWithProductByIdIn(anyCollection()))
                .thenReturn(Collections.singletonList(variant10));
        when(productVariantRepository.decreaseStockQuantity(10, 1)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(null, request);

        verify(emailService, times(1)).sendOrderConfirmationEmail(
                argThat(dto -> dto != null && "buyer@gmail.com".equals("buyer@gmail.com")
                        && dto.getItems() != null && dto.getItems().size() == 1
                        && "Kem Dưỡng".equals(dto.getItems().get(0).getProductName())
                        && "50ml".equals(dto.getItems().get(0).getCapacity())),
                eq("buyer@gmail.com")
        );
    }

    @Test
    void testCancelExpiredBankTransferOrders_WhenStatusStillPending_CancelsAndRollsBackStock() {
        // Given
        ProductVariant variant = ProductVariant.builder().idVariant(10).stockQuantity(5).build();
        com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail detail = 
                com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail.builder()
                        .variant(variant)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100000))
                        .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261025000001")
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .orderDetails(List.of(detail))
                .build();

        when(orderRepository.findByStatusAndPaymentMethodInAndCreatedAtBefore(eq(OrderStatus.PENDING), anyList(), any(java.time.LocalDateTime.class)))
                .thenReturn(List.of(order));
        when(orderRepository.updateStatusIfMatches(eq(1), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), anyString(), any(java.time.LocalDateTime.class)))
                .thenReturn(1);

        // When
        int result = orderService.cancelExpiredBankTransferOrders();

        // Then
        assertEquals(1, result);
        verify(productVariantRepository, times(1)).increaseStockQuantity(10, 2);
    }

    @Test
    void testCancelExpiredBankTransferOrders_WhenWebhookAlreadyProcessed_SkipsRollbackStock() {
        // Given: Webhook received payment concurrently, changing status to PROCESSING
        ProductVariant variant = ProductVariant.builder().idVariant(10).stockQuantity(5).build();
        com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail detail = 
                com.hoangbaoshop.hoangbaocosmetics_backend.entity.OrderDetail.builder()
                        .variant(variant)
                        .quantity(2)
                        .price(BigDecimal.valueOf(100000))
                        .build();

        Order order = Order.builder()
                .idOrder(1)
                .orderCode("HB20261025000001")
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .orderDetails(List.of(detail))
                .build();

        when(orderRepository.findByStatusAndPaymentMethodInAndCreatedAtBefore(eq(OrderStatus.PENDING), anyList(), any(java.time.LocalDateTime.class)))
                .thenReturn(List.of(order));
        // Atomic update returns 0 because status is no longer PENDING (Webhook changed it)
        when(orderRepository.updateStatusIfMatches(eq(1), eq(OrderStatus.PENDING), eq(OrderStatus.CANCELLED), anyString(), any(java.time.LocalDateTime.class)))
                .thenReturn(0);

        // When
        int result = orderService.cancelExpiredBankTransferOrders();

        // Then: 0 orders cancelled by scheduler, and stock is NEVER rolled back!
        assertEquals(0, result);
        verify(productVariantRepository, never()).increaseStockQuantity(anyInt(), anyInt());
    }

    @Test
    void testCancelOrderAdmin_WhenAlreadyCancelled_ThrowsExceptionAndNeverRollsBackStock() {
        // Given
        Order cancelledOrder = Order.builder()
                .idOrder(1)
                .orderCode("HB20261025000001")
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(cancelledOrder));

        com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CancelOrderRequest request = 
                new com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.order.CancelOrderRequest();
        request.setReason("Khách muốn đổi");

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException.class,
                () -> orderService.cancelOrderAdmin(1, request, "admin_user")
        );

        verify(productVariantRepository, never()).increaseStockQuantity(anyInt(), anyInt());
    }
}
