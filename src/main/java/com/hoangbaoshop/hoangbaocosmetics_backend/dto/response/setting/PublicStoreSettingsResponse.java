package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.setting;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicStoreSettingsResponse {
    private BigDecimal defaultShippingFee;
    private BigDecimal freeShippingThreshold;
    private String storeName;
    private String storeHotline;
    private String storeEmail;
}
