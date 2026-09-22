package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteToggleResponse;
import org.springframework.data.domain.Pageable;

public interface FavoriteProductService {

    FavoriteToggleResponse toggleFavorite(String username, Integer productId);

    PageResponse<FavoriteResponse> getMyFavorites(String username, Pageable pageable);

    boolean checkIsFavorite(String username, Integer productId);
}
