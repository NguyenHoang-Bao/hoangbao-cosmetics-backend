package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.SupplierRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.SupplierResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    // tạo nhà cung cấp mới
    SupplierResponse createSupplier(SupplierRequest request);

    // sửa thông tin nhà cung cấp
    SupplierResponse updateSupplier(Integer id, SupplierRequest request);

    // xóa nhà cung cấp
    void deleteSupplier(Integer id);

    // lấy chi tiết nhà cung cấp theo id
    SupplierResponse getSupplierById(Integer id);

    // lấy danh sách nhà cung cấp (có tìm kiếm & phân trang)
    PageResponse<SupplierResponse> getAllSuppliers(String keyword, Pageable pageable);
}
