package com.hoangbaoshop.hoangbaocosmetics_backend.repository;

import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ImportReceiptDetail;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ImportReceiptDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportReceiptDetailRepository extends JpaRepository<ImportReceiptDetail, ImportReceiptDetailId> {

    List<ImportReceiptDetail> findByIdIdReceipt(Integer idReceipt);
}
