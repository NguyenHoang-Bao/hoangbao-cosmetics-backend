package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.warehouse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {

    private Integer idSupplier;
    private String name;
    private String phone;
    private String address;
}
