package com.hoangbaoshop.hoangbaocosmetics_backend.client;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.oauth.OAuthProfile;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GoogleClient {

    @Value("${google.client-id}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuthProfile verifyAndGetProfile(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Google Token không được để trống!");
        }

        // 1. Thử xác thực dưới dạng Google ID Token
        OAuthProfile profile = verifyGoogleIdToken(token);
        if (profile != null) {
            return profile;
        }

        // 2. Nếu không phải ID Token, xác thực dưới dạng Access Token qua /tokeninfo
        return verifyGoogleAccessToken(token);
    }

    private OAuthProfile verifyGoogleIdToken(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(token);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String rawEmail = payload.getEmail();
                String email = (rawEmail != null && !rawEmail.isBlank())
                        ? rawEmail.trim().toLowerCase()
                        : payload.getSubject() + "@google.com";

                return OAuthProfile.builder()
                        .providerId(payload.getSubject())
                        .email(email)
                        .fullName((String) payload.get("name"))
                        .avatarUrl((String) payload.get("picture"))
                        .provider("GOOGLE")
                        .build();
            }
        } catch (Exception ignored) {
            // Không phải ID Token, chuyển sang fallback Access Token
        }
        return null;
    }

    private OAuthProfile verifyGoogleAccessToken(String token) {
        try {
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = restTemplate.getForObject(tokenInfoUrl, Map.class);

            if (tokenInfo != null) {
                // Google /tokeninfo endpoint trả về "audience" và "issued_to" cho OAuth2 Access Token,
                // hoặc "aud" và "azp" đối với JWT tokeninfo. Chúng ta kiểm tra linh hoạt tất cả các key.
                String tokenAud = tokenInfo.get("audience") != null
                        ? String.valueOf(tokenInfo.get("audience"))
                        : (tokenInfo.get("aud") != null ? String.valueOf(tokenInfo.get("aud")) : null);

                String tokenAzp = tokenInfo.get("issued_to") != null
                        ? String.valueOf(tokenInfo.get("issued_to"))
                        : (tokenInfo.get("azp") != null ? String.valueOf(tokenInfo.get("azp")) : null);

                boolean isAudienceValid = (googleClientId != null && !googleClientId.isBlank()) &&
                        (googleClientId.equals(tokenAud) || googleClientId.equals(tokenAzp));

                if (!isAudienceValid) {
                    log.warn("Google Access Token bị từ chối: Token Client ID ('{}' / '{}') không khớp với Server Client ID '{}'",
                            tokenAud, tokenAzp, googleClientId);
                    throw new BadRequestException("Google Token không thuộc về ứng dụng này!");
                }

                String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + token;
                @SuppressWarnings("unchecked")
                Map<String, Object> googleResponse = restTemplate.getForObject(googleUserInfoUrl, Map.class);

                if (googleResponse != null && googleResponse.get("sub") != null) {
                    String sub = (String) googleResponse.get("sub");
                    String rawEmail = (String) googleResponse.get("email");
                    String email = (rawEmail != null && !rawEmail.isBlank())
                            ? rawEmail.trim().toLowerCase()
                            : sub + "@google.com";

                    return OAuthProfile.builder()
                            .providerId(sub)
                            .email(email)
                            .fullName((String) googleResponse.get("name"))
                            .avatarUrl((String) googleResponse.get("picture"))
                            .provider("GOOGLE")
                            .build();
                }
            }
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception ex) {
            log.error("Lỗi khi xác thực Google Access Token: {}", ex.getMessage());
        }

        throw new BadRequestException("Google Token không hợp lệ hoặc đã hết hạn!");
    }
}
