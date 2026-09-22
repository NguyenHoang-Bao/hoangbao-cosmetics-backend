package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.warehouse.SupplierRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse.SupplierResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Supplier;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ImportReceiptRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ImportReceiptRepository importReceiptRepository;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên nhà cung cấp đã tồn tại: " + request.getName());
        }

        Supplier supplier = Supplier.builder()
                .name(request.getName().trim())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return mapToSupplierResponse(saved);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Integer id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));

        if (!supplier.getName().equalsIgnoreCase(request.getName().trim()) &&
                supplierRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên nhà cung cấp đã tồn tại: " + request.getName());
        }

        supplier.setName(request.getName().trim());
        supplier.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        supplier.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

        Supplier updated = supplierRepository.save(supplier);
        return mapToSupplierResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSupplier(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));

        if (importReceiptRepository.existsBySupplierIdSupplier(id)) {
            throw new BadRequestException("Không thể xóa nhà cung cấp này vì đã có phiếu nhập kho liên kết!");
        }

        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        return mapToSupplierResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> getAllSuppliers(String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Supplier> page = supplierRepository.searchSuppliers(kw, pageable);
        List<SupplierResponse> content = page.getContent().stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());

        return PageResponse.<SupplierResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .idSupplier(supplier.getIdSupplier())
                .name(supplier.getName())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .build();
    }
}
