package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.customer;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLookupResponse {
    private Integer idUser;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Integer loyaltyPoints;
    private String membershipTier; // BẠC, VÀNG, KIM CƯƠNG
    private Long totalOrders;
    private Double totalSpent;
}
