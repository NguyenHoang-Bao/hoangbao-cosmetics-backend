package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "import_price", nullable = false)
    private Double importPrice;
}
