package com.hoangbaoshop.hoangbaocosmetics_backend.dto.oauth;

import lombok.Builder;

@Builder
public record OAuthProfile(
                String providerId,
                String email,
                String fullName,
                String avatarUrl,
                String provider // fb hoac gg
) {
}
