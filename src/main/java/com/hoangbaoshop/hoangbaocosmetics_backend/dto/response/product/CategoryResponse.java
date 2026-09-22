package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Integer idCategory;
    private String name;
    private String slug;
}
