package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review.ReviewRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ProductReviewSummaryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ReviewResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderDetailRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ReviewRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void testCreateReview_Success() {
        User user = User.builder().idUser(1).username("customer1").fullName("Khách hàng 1").build();
        Product product = Product.builder().idProduct(10).name("Son môi").build();
        ProductVariant variant = ProductVariant.builder().idVariant(5).product(product).build();

        Order order = Order.builder()
                .idOrder(100)
                .user(user)
                .status(OrderStatus.DELIVERED)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .idOrderDetail(50)
                .order(order)
                .variant(variant)
                .isReviewed(false)
                .build();

        ReviewRequest request = ReviewRequest.builder()
                .idProduct(10)
                .idOrderDetail(50)
                .ratingPoint(5.0f)
                .content("Sản phẩm rất tốt!")
                .build();

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(orderDetailRepository.findById(50)).thenReturn(Optional.of(orderDetail));
        when(reviewRepository.existsByOrderDetailIdOrderDetail(50)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview("customer1", request);

        assertNotNull(response);
        assertEquals(5.0f, response.getRatingPoint());
        assertEquals("Sản phẩm rất tốt!", response.getContent());
        assertTrue(orderDetail.getIsReviewed());
        verify(orderDetailRepository, times(1)).save(orderDetail);
    }

    @Test
    void testCreateReview_OrderNotDelivered_ThrowsBadRequestException() {
        User user = User.builder().idUser(1).username("customer1").build();
        Product product = Product.builder().idProduct(10).build();
        Order order = Order.builder().idOrder(100).user(user).status(OrderStatus.SHIPPING).build();
        OrderDetail orderDetail = OrderDetail.builder().idOrderDetail(50).order(order).build();

        ReviewRequest request = ReviewRequest.builder().idProduct(10).idOrderDetail(50).build();

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(orderDetailRepository.findById(50)).thenReturn(Optional.of(orderDetail));

        assertThrows(BadRequestException.class, () -> reviewService.createReview("customer1", request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_AlreadyReviewed_ThrowsBadRequestException() {
        User user = User.builder().idUser(1).username("customer1").build();
        Product product = Product.builder().idProduct(10).build();
        Order order = Order.builder().idOrder(100).user(user).status(OrderStatus.DELIVERED).build();
        OrderDetail orderDetail = OrderDetail.builder().idOrderDetail(50).order(order).isReviewed(true).build();

        ReviewRequest request = ReviewRequest.builder().idProduct(10).idOrderDetail(50).build();

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(orderDetailRepository.findById(50)).thenReturn(Optional.of(orderDetail));

        assertThrows(BadRequestException.class, () -> reviewService.createReview("customer1", request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetProductReviews_CalculatesSummaryAndReturnsPagedReviews() {
        Pageable pageable = PageRequest.of(0, 5);
        when(productRepository.existsById(10)).thenReturn(true);
        when(reviewRepository.countByProductIdProduct(10)).thenReturn(2L);
        when(reviewRepository.calculateAverageRatingByProductId(10)).thenReturn(4.5);

        Review review1 = Review.builder().idReview(1).ratingPoint(5.0f).content("Tốt").build();
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review1), pageable, 1);
        when(reviewRepository.findByProductIdProduct(10, pageable)).thenReturn(reviewPage);

        ProductReviewSummaryResponse summary = reviewService.getProductReviews(10, pageable);

        assertNotNull(summary);
        assertEquals(4.5, summary.getAverageRating());
        assertEquals(2L, summary.getTotalReviews());
        assertEquals(1, summary.getReviews().getContent().size());
    }
}
