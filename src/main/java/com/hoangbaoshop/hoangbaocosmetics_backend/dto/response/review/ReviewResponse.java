package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.review;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Integer idReview;
    private Integer idProduct;
    private String productName;
    private Integer idUser;
    private String userFullName;
    private String userAvatar;
    private Integer idOrderDetail;
    private Float ratingPoint;
    private String content;
    private LocalDateTime createdAt;
}
