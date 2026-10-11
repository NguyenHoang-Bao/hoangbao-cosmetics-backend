package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "import_receipt_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptDetail {

    @EmbeddedId
    @Builder.Default
    private ImportReceiptDetailId id = new ImportReceiptDetailId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idReceipt")
    @JoinColumn(name = "id_receipt")
    private ImportReceipt importReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idVariant")
    @JoinColumn(name = "id_variant")
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(name = "shelf_quantity")
    private Integer shelfQuantity = 0;

    @Builder.Default
    @Column(name = "central_quantity")
    private Integer centralQuantity = 0;

    @Column(name = "import_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;
}
