package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

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

    @Column(name = "capacity", nullable = false, length = 50)
    private String capacity;

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "price", nullable = false)
    private Double price;

    @Builder.Default
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
}
