package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {

    private Integer idProduct;
    private String productName;
    private String thumbnail;
    private String categoryName;
    private String brandName;
    private Double minPrice;
    private LocalDateTime favoritedAt;
}
