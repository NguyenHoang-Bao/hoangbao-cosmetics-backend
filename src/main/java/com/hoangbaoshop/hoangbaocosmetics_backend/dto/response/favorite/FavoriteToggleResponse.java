package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteToggleResponse {

    private Integer idProduct;
    private boolean isFavorite;
    private String message;
}
