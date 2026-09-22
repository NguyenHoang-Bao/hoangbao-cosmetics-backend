package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.SupplierRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.SupplierResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest request);

    SupplierResponse updateSupplier(Integer id, SupplierRequest request);

    void deleteSupplier(Integer id);

    SupplierResponse getSupplierById(Integer id);

    PageResponse<SupplierResponse> getAllSuppliers(String keyword, Pageable pageable);
}
