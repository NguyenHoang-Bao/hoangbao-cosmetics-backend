package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "replenishment_slips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplenishmentSlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_slip")
    private Integer idSlip;

    @Column(name = "slip_code", nullable = false, unique = true, length = 50)
    private String slipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_performer", nullable = false)
    private User performer;

    @Builder.Default
    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Builder.Default
    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "COMPLETED";

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "replenishmentSlip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReplenishmentSlipDetail> details = new ArrayList<>();

    //lấy now hiện tại lưu xuôgs db
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
