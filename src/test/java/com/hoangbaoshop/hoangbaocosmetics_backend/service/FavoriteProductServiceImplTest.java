package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.favorite.FavoriteToggleResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.*;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FavoriteProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteProductServiceImplTest {

    @Mock
    private FavoriteProductRepository favoriteProductRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FavoriteProductServiceImpl favoriteProductService;

    @Test
    void testToggleFavorite_WhenNotFavorited_AddsFavorite() {
        User user = User.builder().idUser(1).username("customer1").build();
        Product product = Product.builder().idProduct(10).name("Kem chống nắng").build();

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(favoriteProductRepository.findByIdIdUserAndIdIdProduct(1, 10)).thenReturn(Optional.empty());

        FavoriteToggleResponse response = favoriteProductService.toggleFavorite("customer1", 10);

        assertNotNull(response);
        assertTrue(response.isFavorite());
        assertEquals(10, response.getIdProduct());
        verify(favoriteProductRepository, times(1)).save(any(FavoriteProduct.class));
        verify(favoriteProductRepository, never()).delete(any());
    }

    @Test
    void testToggleFavorite_WhenAlreadyFavorited_RemovesFavorite() {
        User user = User.builder().idUser(1).username("customer1").build();
        Product product = Product.builder().idProduct(10).name("Kem chống nắng").build();
        FavoriteProduct existing = FavoriteProduct.builder()
                .id(new FavoriteProductId(1, 10))
                .user(user)
                .product(product)
                .build();

        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(productRepository.findById(10)).thenReturn(Optional.of(product));
        when(favoriteProductRepository.findByIdIdUserAndIdIdProduct(1, 10)).thenReturn(Optional.of(existing));

        FavoriteToggleResponse response = favoriteProductService.toggleFavorite("customer1", 10);

        assertNotNull(response);
        assertFalse(response.isFavorite());
        verify(favoriteProductRepository, times(1)).delete(existing);
        verify(favoriteProductRepository, never()).save(any());
    }

    @Test
    void testGetUserFavorites_ReturnsPagedResponse() {
        User user = User.builder().idUser(1).username("customer1").build();
        Product product = Product.builder()
                .idProduct(10)
                .name("Kem chống nắng")
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();
        FavoriteProduct fav = FavoriteProduct.builder().id(new FavoriteProductId(1, 10)).user(user).product(product).build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<FavoriteProduct> page = new PageImpl<>(Collections.singletonList(fav), pageable, 1);

        when(favoriteProductRepository.findByUserUsername(eq("customer1"), any(Pageable.class))).thenReturn(page);

        PageResponse<FavoriteResponse> response = favoriteProductService.getMyFavorites("customer1", pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(10, response.getContent().get(0).getIdProduct());
    }

    @Test
    void testCheckIsFavorite_ReturnsTrue() {
        User user = User.builder().idUser(1).username("customer1").build();
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(user));
        when(favoriteProductRepository.existsByIdIdUserAndIdIdProduct(1, 10)).thenReturn(true);

        boolean result = favoriteProductService.checkIsFavorite("customer1", 10);

        assertTrue(result);
    }
}
