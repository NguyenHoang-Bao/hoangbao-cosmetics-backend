package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponse {

    private Integer idBrand;
    private String name;
    private String logoUrl;
}
