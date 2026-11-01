package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.flashsale.FlashSaleRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.flashsale.FlashSaleResponse;

import java.util.List;

public interface FlashSaleService {

    FlashSaleResponse getActiveFlashSale();

    List<FlashSaleResponse> getAllFlashSales();

    FlashSaleResponse getFlashSaleById(Integer id);

    FlashSaleResponse createFlashSale(FlashSaleRequest request);

    void deleteFlashSale(Integer id);
}
