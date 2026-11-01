package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Integer> {

    List<FlashSaleItem> findByFlashSale_IdFlashSale(Integer idFlashSale);

    Optional<FlashSaleItem> findByFlashSale_IdFlashSaleAndVariant_IdVariant(Integer idFlashSale, Integer idVariant);
}
