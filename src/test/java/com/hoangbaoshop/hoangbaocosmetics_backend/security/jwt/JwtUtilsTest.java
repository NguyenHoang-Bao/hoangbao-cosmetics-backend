package com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L); // 1 giờ
    }

    @Test
    void testGenerateAndValidateToken() {
        String username = "hoangbao_admin";
        String token = jwtUtils.generateTokenFromUsername(username);

        assertNotNull(token, "Token không được null");
        assertTrue(jwtUtils.validateJwtToken(token), "Token phải hợp lệ");
        assertEquals(username, jwtUtils.getUserNameFromJwtToken(token), "Username giải mã phải trùng khớp");
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalid.signature";
        assertFalse(jwtUtils.validateJwtToken(invalidToken), "Token sai phải trả về false");
    }
}
