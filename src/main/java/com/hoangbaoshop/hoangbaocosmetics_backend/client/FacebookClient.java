package com.hoangbaoshop.hoangbaocosmetics_backend.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.oauth.OAuthProfile;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FacebookClient {

    @Value("${facebook.app-id}")
    private String facebookAppId;

    @Value("${facebook.app-secret:}")
    private String facebookAppSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public OAuthProfile verifyAndGetProfile(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadRequestException("Facebook Access Token không được để trống!");
        }

        // 1. Thẩm định quyền sở hữu Token qua /debug_token (Chặn Confused Deputy
        // Attack)
        verifyTokenOwnership(accessToken);

        // 2. Lấy thông tin tài khoản qua Graph API /me
        return fetchUserProfile(accessToken);
    }

    private void verifyTokenOwnership(String accessToken) {
        if (facebookAppSecret != null && !facebookAppSecret.isBlank()) {
            try {
                String appAccessToken = facebookAppId + "|" + facebookAppSecret;
                String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken
                        + "&access_token=" + appAccessToken;

                @SuppressWarnings("unchecked")
                Map<String, Object> debugResponse = restTemplate.getForObject(debugUrl, Map.class);

                if (debugResponse != null && debugResponse.get("data") instanceof Map<?, ?> dataMap) {
                    Boolean isValid = (Boolean) dataMap.get("is_valid");
                    String tokenAppId = String.valueOf(dataMap.get("app_id"));

                    if (!Boolean.TRUE.equals(isValid)) {
                        throw new BadRequestException("Facebook Token không hợp lệ hoặc đã hết hạn!");
                    }

                    if (!facebookAppId.equals(tokenAppId)) {
                        log.warn("CẢNH BÁO BẢO MẬT: Token App ID '{}' không khớp với Shop App ID '{}'", tokenAppId,
                                facebookAppId);
                        throw new BadRequestException("Token này được cấp bởi ứng dụng khác, không thuộc về hệ thống!");
                    }
                }
            } catch (BadRequestException bre) {
                throw bre;
            } catch (Exception ex) {
                log.error("Lỗi khi gọi Facebook Debug Token: {}", ex.getMessage());
                throw new BadRequestException("Không thể xác thực tính hợp lệ của Facebook Token!");
            }
        }
    }

    private OAuthProfile fetchUserProfile(String accessToken) {
        try {
            String fbGraphUrl = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token="
                    + accessToken;
            @SuppressWarnings("unchecked")
            Map<String, Object> fbResponse = restTemplate.getForObject(fbGraphUrl, Map.class);

            if (fbResponse == null || fbResponse.get("id") == null) {
                throw new BadRequestException("Không thể lấy thông tin tài khoản từ Facebook!");
            }

            String fbId = (String) fbResponse.get("id");
            String fullName = (String) fbResponse.get("name");
            String email = (String) fbResponse.get("email");

            // Xử lý tài khoản Facebook đăng ký bằng SĐT không có email
            if (email == null || email.isBlank()) {
                email = fbId + "@facebook.com";
            }

            // Lấy link ảnh đại diện
            String pictureUrl = null;
            if (fbResponse.get("picture") instanceof Map<?, ?> pictureMap) {
                if (pictureMap.get("data") instanceof Map<?, ?> dataMap) {
                    pictureUrl = (String) dataMap.get("url");
                }
            }

            return OAuthProfile.builder()
                    .providerId(fbId)
                    .email(email.trim().toLowerCase())
                    .fullName(fullName != null ? fullName : "Khách hàng Facebook")
                    .avatarUrl(pictureUrl)
                    .provider("FACEBOOK")
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin người dùng từ Facebook: {}", e.getMessage());
            throw new BadRequestException("Đăng nhập bằng Facebook thất bại: " + e.getMessage());
        }
    }
}