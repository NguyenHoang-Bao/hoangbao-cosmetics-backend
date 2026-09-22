package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.review.ReviewRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ProductReviewSummaryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review.ReviewResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {

    private final ReviewService reviewService;

    // 1. Viết đánh giá cho sản phẩm đã mua (Yêu cầu đăng nhập)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đánh giá sản phẩm thành công!"));
    }

    // 2. Lấy danh sách đánh giá của một sản phẩm kèm điểm trung bình (Công khai)
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductReviewSummaryResponse>> getProductReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        ProductReviewSummaryResponse response = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Lấy danh sách đánh giá của chính người dùng đăng nhập
    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<ReviewResponse> response = reviewService.getMyReviews(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. Admin / Staff xóa đánh giá vi phạm
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteReviewAdmin(@PathVariable Integer id) {
        reviewService.deleteReviewAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa đánh giá thành công!"));
    }
}
