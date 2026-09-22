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
public class FavoriteProductId implements Serializable {

    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "id_product")
    private Integer idProduct;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteProductId that = (FavoriteProductId) o;
        return Objects.equals(idUser, that.idUser) && Objects.equals(idProduct, that.idProduct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idProduct);
    }
}
