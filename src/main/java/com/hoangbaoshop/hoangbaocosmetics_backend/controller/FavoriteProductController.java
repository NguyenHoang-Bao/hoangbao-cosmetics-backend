package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteToggleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.FavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class FavoriteProductController {

    private final FavoriteProductService favoriteProductService;

    // 1. Thêm hoặc Bỏ yêu thích sản phẩm (Toggle)
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<FavoriteToggleResponse>> toggleFavorite(
            @PathVariable Integer productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FavoriteToggleResponse response = favoriteProductService.toggleFavorite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    // 2. Lấy danh sách sản phẩm yêu thích của người dùng đăng nhập
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<FavoriteResponse> response = favoriteProductService.getMyFavorites(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3. Kiểm tra sản phẩm đã được yêu thích chưa (Dùng cho nút bấm trái tim ở trang chi tiết)
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkIsFavorite(
            @PathVariable Integer productId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean isFav = favoriteProductService.checkIsFavorite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success(isFav));
    }
}
