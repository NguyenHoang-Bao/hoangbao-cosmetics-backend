package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.JwtUtils;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private OtpService otpService;
    @Mock
    private com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository otpTokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testRegister_Success_EncodesPasswordAndAssignsCustomerRole() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new_user")
                .password("plain_pass")
                .fullName("Nguyễn Văn A")
                .email("  Nva@Gmail.Com  ")
                .phoneNumber("0988888888")
                .build();

        when(userRepository.existsByUsername("new_user")).thenReturn(false);
        when(userRepository.existsByEmail("nva@gmail.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("0988888888")).thenReturn(false);
        when(passwordEncoder.encode("plain_pass")).thenReturn("encoded_pass");

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        when(roleRepository.findByNameRole("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(userRepository, times(1)).save(argThat(user ->
                user.getUsername().equals("new_user") &&
                user.getPassword().equals("encoded_pass") &&
                "nva@gmail.com".equals(user.getEmail()) &&
                user.getRoles().contains(customerRole) &&
                Boolean.TRUE.equals(user.getEnabled())
        ));
        verify(emailService, times(1)).sendAccountVerificationEmail(eq("nva@gmail.com"), eq("Nguyễn Văn A"), anyString());
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder().username("existing_user").build();
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder().username("user2").email("dup@gmail.com").build();
        when(userRepository.existsByUsername("user2")).thenReturn(false);
        when(userRepository.existsByEmail("dup@gmail.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success_ReturnsJwtAndUserDetails() {
        LoginRequest request = LoginRequest.builder()
                .username("test_user")
                .password("password123")
                .build();

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .idUser(10)
                .username("test_user")
                .email("test@gmail.com")
                .authorities(Collections.emptyList())
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("sample_jwt_token_123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("sample_jwt_token_123", response.getAccessToken());
        assertEquals("test_user", response.getUsername());
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("test_user")
                .password("wrong_password")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Sai tài khoản hoặc mật khẩu"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testVerifyEmailToken_Success_SetsEmailVerifiedTrue() {
        String token = "valid_uuid_token";
        com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken otpToken = com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken.builder()
                .email("test@gmail.com")
                .otpCode(token)
                .tokenType(com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType.REGISTER_VERIFY)
                .expiryTime(java.time.LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        User user = User.builder().email("test@gmail.com").emailVerified(false).build();

        when(otpTokenRepository.findTopByOtpCodeAndTokenTypeAndIsUsedFalse(token, com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType.REGISTER_VERIFY))
                .thenReturn(Optional.of(otpToken));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        authService.verifyEmailToken(token);

        assertTrue(otpToken.getIsUsed());
        assertTrue(user.getEmailVerified());
        verify(userRepository, times(1)).save(user);
    }
}
