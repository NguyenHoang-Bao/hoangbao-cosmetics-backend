package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.user;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Integer idUser;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatar;
    private Integer loyaltyPoints;
    private String authProvider;
    private Boolean enabled;
    private Boolean emailVerified;
    private Set<String> roles;
}
