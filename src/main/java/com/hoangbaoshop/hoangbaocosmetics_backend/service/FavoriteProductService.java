package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteToggleResponse;
import org.springframework.data.domain.Pageable;

public interface FavoriteProductService {

    // bấm thả tim (nếu chưa thích thì thêm, nếu thích rồi thì bỏ thích)
    FavoriteToggleResponse toggleFavorite(String username, Integer productId);

    // lấy danh sách sản phẩm đã thích của tôi (có phân trang)
    PageResponse<FavoriteResponse> getMyFavorites(String username, Pageable pageable);

    // kiểm tra xem tôi đã thích sản phẩm này chưa (để tô màu tim đỏ/xám trên giao diện)
    boolean checkIsFavorite(String username, Integer productId);
}
