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
                .price(100000.0)
                .stockQuantity(10)
                .build();
        ProductVariant variant50 = ProductVariant.builder()
                .idVariant(50)
                .product(product)
                .price(200000.0)
                .stockQuantity(10)
                .build();

        when(productVariantRepository.findAllWithProductByIdIn(anyCollection()))
                .thenReturn(Arrays.asList(variant20, variant50));
        when(productVariantRepository.decreaseStockQuantity(20, 1)).thenReturn(1);
        when(productVariantRepository.decreaseStockQuantity(50, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.createOrder("customer_user", request);

        // Then: verify lock/decreaseStock calls were executed strictly in ascending ID order (20 then 50)
        InOrder inOrder = inOrder(productVariantRepository);
        inOrder.verify(productVariantRepository).decreaseStockQuantity(20, 1);
        inOrder.verify(productVariantRepository).decreaseStockQuantity(50, 2);

        // Verify single batch load query
        verify(productVariantRepository, times(1)).findAllWithProductByIdIn(anyCollection());
        verify(inventoryLogRepository, times(1)).saveAll(anyList());
    }
}
