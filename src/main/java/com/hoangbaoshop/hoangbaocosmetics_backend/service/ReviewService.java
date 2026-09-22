package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review.ReviewRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ProductReviewSummaryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(String username, ReviewRequest request);

    ProductReviewSummaryResponse getProductReviews(Integer productId, Pageable pageable);

    PageResponse<ReviewResponse> getMyReviews(String username, Pageable pageable);

    void deleteReviewAdmin(Integer reviewId);
}
