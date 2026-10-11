package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review.ReviewRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ProductReviewSummaryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    // viết đánh giá sản phẩm (chấm sao 1-5 + bình luận)
    ReviewResponse createReview(String username, ReviewRequest request);

    // lấy đánh giá của sản phẩm (kèm điểm sao trung bình & phân trang)
    ProductReviewSummaryResponse getProductReviews(Integer productId, Pageable pageable);

    // lấy lịch sử đánh giá của chính tôi
    PageResponse<ReviewResponse> getMyReviews(String username, Pageable pageable);

    // admin xóa đánh giá xấu / vi phạm
    void deleteReviewAdmin(Integer reviewId);
}
