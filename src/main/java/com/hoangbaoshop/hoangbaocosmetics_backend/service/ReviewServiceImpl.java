package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review.ReviewRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ProductReviewSummaryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ReviewResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OrderStatus;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OrderDetailRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ReviewRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(String username, ReviewRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng!"));

        Product product = productRepository.findById(request.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + request.getIdProduct()));

        OrderDetail orderDetail = orderDetailRepository.findById(request.getIdOrderDetail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đơn hàng với ID: " + request.getIdOrderDetail()));

        Order order = orderDetail.getOrder();

        // 1. Kiểm tra đơn hàng có thuộc sở hữu của người dùng hiện tại không
        if (order.getUser() == null || !order.getUser().getIdUser().equals(user.getIdUser())) {
            throw new BadRequestException("Bạn không có quyền đánh giá sản phẩm không thuộc đơn hàng của mình!");
        }

        // 2. Kiểm tra trạng thái đơn hàng (phải là DELIVERED)
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ có thể đánh giá sản phẩm khi đơn hàng đã được giao thành công (DELIVERED)!");
        }

        // 3. Kiểm tra sản phẩm trong đơn đã được đánh giá chưa
        if (Boolean.TRUE.equals(orderDetail.getIsReviewed()) || reviewRepository.existsByOrderDetailIdOrderDetail(orderDetail.getIdOrderDetail())) {
            throw new BadRequestException("Sản phẩm trong đơn hàng này đã được đánh giá trước đó rồi!");
        }

        // 4. Kiểm tra biến thể trong chi tiết đơn hàng có khớp với ID sản phẩm không
        if (orderDetail.getVariant() == null || orderDetail.getVariant().getProduct() == null ||
                !orderDetail.getVariant().getProduct().getIdProduct().equals(product.getIdProduct())) {
            throw new BadRequestException("Sản phẩm trong đơn hàng không khớp với sản phẩm được đánh giá!");
        }

        // 5. Lưu đánh giá và cập nhật cờ is_reviewed = true
        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderDetail(orderDetail)
                .ratingPoint(request.getRatingPoint())
                .content(request.getContent() != null ? request.getContent().trim() : null)
                .build();

        Review savedReview = reviewRepository.save(review);

        orderDetail.setIsReviewed(true);
        orderDetailRepository.save(orderDetail);

        return mapToReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewSummaryResponse getProductReviews(Integer productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        Page<Review> page = reviewRepository.findByProductIdProduct(productId, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());

        PageResponse<ReviewResponse> pageResponse = PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        Double rawAvg = reviewRepository.calculateAverageRatingByProductId(productId);
        double roundedAvg = rawAvg != null ? Math.round(rawAvg * 10.0) / 10.0 : 0.0;
        long totalReviews = reviewRepository.countByProductIdProduct(productId);

        return ProductReviewSummaryResponse.builder()
                .productId(productId)
                .averageRating(roundedAvg)
                .totalReviews(totalReviews)
                .reviews(pageResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(String username, Pageable pageable) {
        Page<Review> page = reviewRepository.findByUserUsername(username, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteReviewAdmin(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        reviewRepository.delete(review);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        User user = review.getUser();
        Product product = review.getProduct();
        OrderDetail detail = review.getOrderDetail();

        return ReviewResponse.builder()
                .idReview(review.getIdReview())
                .idProduct(product != null ? product.getIdProduct() : null)
                .productName(product != null ? product.getName() : null)
                .idUser(user != null ? user.getIdUser() : null)
                .userFullName(user != null ? user.getFullName() : null)
                .userAvatar(user != null ? user.getAvatar() : null)
                .idOrderDetail(detail != null ? detail.getIdOrderDetail() : null)
                .ratingPoint(review.getRatingPoint())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
