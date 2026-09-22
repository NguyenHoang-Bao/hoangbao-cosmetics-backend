package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receipt")
    private Integer idReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supplier", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_warehouse_manager", nullable = false)
    private User warehouseManager;

    @Column(name = "import_date")
    private LocalDateTime importDate;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "note", length = 255)
    private String note;

    @Builder.Default
    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportReceiptDetail> details = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.importDate == null) {
            this.importDate = LocalDateTime.now();
        }
    }
}
