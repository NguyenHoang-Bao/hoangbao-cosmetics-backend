package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FavoriteProduct;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FavoriteProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteProductRepository extends JpaRepository<FavoriteProduct, FavoriteProductId> {

    Page<FavoriteProduct> findByUserUsername(String username, Pageable pageable);

    boolean existsByIdIdUserAndIdIdProduct(Integer idUser, Integer idProduct);

    Optional<FavoriteProduct> findByIdIdUserAndIdIdProduct(Integer idUser, Integer idProduct);
}
