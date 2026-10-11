package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variant")
    private Integer idVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "capacity", nullable = false, length = 50)
    private String capacity;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Builder.Default
    @Column(name = "shelf_stock")
    private Integer shelfStock = 0;

    @Builder.Default
    @Column(name = "central_stock")
    private Integer centralStock = 0;

    @Builder.Default
    @Column(name = "shelf_min_alert")
    private Integer shelfMinAlert = 5;

    @Builder.Default
    @Column(name = "shelf_capacity")
    private Integer shelfCapacity = 20;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
