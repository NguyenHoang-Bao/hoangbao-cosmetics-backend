package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteToggleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FavoriteProduct;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FavoriteProductId;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FavoriteProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteProductServiceImpl implements FavoriteProductService {

    private final FavoriteProductRepository favoriteProductRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public FavoriteToggleResponse toggleFavorite(String username, Integer productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng!"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        Optional<FavoriteProduct> existingFav = favoriteProductRepository.findByIdIdUserAndIdIdProduct(user.getIdUser(), productId);

        if (existingFav.isPresent()) {
            favoriteProductRepository.delete(existingFav.get());
            return FavoriteToggleResponse.builder()
                    .idProduct(productId)
                    .isFavorite(false)
                    .message("Đã xóa sản phẩm khỏi danh sách yêu thích")
                    .build();
        } else {
            FavoriteProduct fav = FavoriteProduct.builder()
                    .id(new FavoriteProductId(user.getIdUser(), productId))
                    .user(user)
                    .product(product)
                    .build();
            favoriteProductRepository.save(fav);
            return FavoriteToggleResponse.builder()
                    .idProduct(productId)
                    .isFavorite(true)
                    .message("Đã thêm sản phẩm vào danh sách yêu thích")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getMyFavorites(String username, Pageable pageable) {
        Page<FavoriteProduct> page = favoriteProductRepository.findByUserUsername(username, pageable);
        List<FavoriteResponse> content = page.getContent().stream()
                .map(this::mapToFavoriteResponse)
                .collect(Collectors.toList());

        return PageResponse.<FavoriteResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkIsFavorite(String username, Integer productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng!"));
        return favoriteProductRepository.existsByIdIdUserAndIdIdProduct(user.getIdUser(), productId);
    }

    private FavoriteResponse mapToFavoriteResponse(FavoriteProduct fav) {
        Product product = fav.getProduct();
        Double minPrice = 0.0;
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            minPrice = product.getVariants().stream()
                    .mapToDouble(ProductVariant::getPrice)
                    .min()
                    .orElse(0.0);
        }

        return FavoriteResponse.builder()
                .idProduct(product.getIdProduct())
                .productName(product.getName())
                .thumbnail(product.getThumbnail())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .minPrice(minPrice)
                .favoritedAt(fav.getCreatedAt())
                .build();
    }
}
