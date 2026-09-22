package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.GlobalExceptionHandler;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testRegisterValidationFailure_ReturnsBadRequestWithApiResponse() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("") // Blank username
                .password("123") // Too short
                .fullName("") // Blank fullName
                .email("invalid-email") // Invalid email
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data.username").isNotEmpty())
                .andExpect(jsonPath("$.data.password").isNotEmpty())
                .andExpect(jsonPath("$.data.fullName").isNotEmpty())
                .andExpect(jsonPath("$.data.email").isNotEmpty());

        verify(authService, never()).register(any());
    }

    @Test
    void testRegisterDuplicateUsername_ReturnsBadRequestWithApiResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("123456")
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phoneNumber("0912345678")
                .build();

        doThrow(new BadRequestException("Tên đăng nhập đã tồn tại!"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tên đăng nhập đã tồn tại!"));
    }

    @Test
    void testRegisterSuccess_ReturnsCreatedWithApiResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("123456")
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phoneNumber("0912345678")
                .build();

        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký tài khoản thành công!"));
    }

    @Test
    void testLoginBadCredentials_ReturnsUnauthorizedWithApiResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không chính xác!"));
    }

    @Test
    void testLoginSuccess_ReturnsOkWithApiResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("123456")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock.jwt.token")
                .tokenType("Bearer")
                .idUser(1)
                .username("admin")
                .fullName("Hoàng Bảo Admin")
                .roles(Set.of("ROLE_ADMIN"))
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công!"))
                .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
