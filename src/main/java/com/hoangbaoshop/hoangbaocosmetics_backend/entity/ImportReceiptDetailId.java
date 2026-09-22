package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportReceiptDetailId implements Serializable {

    @Column(name = "id_receipt")
    private Integer idReceipt;

    @Column(name = "id_variant")
    private Integer idVariant;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportReceiptDetailId that = (ImportReceiptDetailId) o;
        return Objects.equals(idReceipt, that.idReceipt) && Objects.equals(idVariant, that.idVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReceipt, idVariant);
    }
}
