package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "replenishment_slip_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplenishmentSlipDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail")
    private Integer idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_slip", nullable = false)
    private ReplenishmentSlip replenishmentSlip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variant", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
