package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewSummaryResponse {

    private Integer productId;
    private Double averageRating;
    private Long totalReviews;
    private PageResponse<ReviewResponse> reviews;
}
