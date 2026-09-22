package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Integer idUser;
    private String username;
    private String fullName;
    private String email;
    private String avatar;
    private Set<String> roles;
}
