package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.client.FacebookClient;
import com.hoangbaoshop.hoangbaocosmetics_backend.client.GoogleClient;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.oauth.OAuthProfile;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    @Mock
    private GoogleClient googleClient;
    @Mock
    private FacebookClient facebookClient;

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
    void testLogin_Success_ReturnsAuthResponseWithJwt() {
        LoginRequest request = LoginRequest.builder()
                .username("test_user")
                .password("password123")
                .build();

        Role role = Role.builder().idRole(1).nameRole("ROLE_USER").build();
        User mockUser = User.builder()
                .idUser(10)
                .username("test_user")
                .fullName("Test User")
                .email("test@example.com")
                .roles(Collections.singleton(role))
                .enabled(true)
                .build();

        UserDetailsImpl userDetails = UserDetailsImpl.build(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

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
    void testLoginWithGoogle_NewUser_CreatesUserWithCustomerRoleAndReturnsJwt() {
        GoogleLoginRequest request = GoogleLoginRequest.builder().idToken("valid_google_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("google_12345")
                .email("newuser@gmail.com")
                .fullName("Google User")
                .avatarUrl("https://example.com/avatar.jpg")
                .provider("GOOGLE")
                .build();

        when(googleClient.verifyAndGetProfile("valid_google_token")).thenReturn(profile);
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        when(roleRepository.findByNameRole("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_uuid_password");

        User savedUser = User.builder()
                .idUser(101)
                .username("newuser@gmail.com")
                .email("newuser@gmail.com")
                .fullName("Google User")
                .avatar("https://example.com/avatar.jpg")
                .authProvider("GOOGLE")
                .providerId("google_12345")
                .enabled(true)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("google_jwt_token_xyz");

        AuthResponse response = authService.loginWithGoogle(request);

        assertNotNull(response);
        assertEquals("google_jwt_token_xyz", response.getAccessToken());
        assertEquals("newuser@gmail.com", response.getEmail());
        assertEquals("Google User", response.getFullName());
        assertTrue(response.getRoles().contains("ROLE_CUSTOMER"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginWithGoogle_ExistingUserWithoutProvider_LinksProviderAndReturnsJwt() {
        GoogleLoginRequest request = GoogleLoginRequest.builder().idToken("valid_google_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("google_12345")
                .email("existing@gmail.com")
                .fullName("Google Existing")
                .avatarUrl("https://example.com/avatar.jpg")
                .provider("GOOGLE")
                .build();

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        User existingUser = User.builder()
                .idUser(102)
                .username("existing@gmail.com")
                .email("existing@gmail.com")
                .fullName("Existing Normal User")
                .authProvider(null)
                .providerId(null)
                .enabled(true)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        when(googleClient.verifyAndGetProfile("valid_google_token")).thenReturn(profile);
        when(userRepository.findByEmail("existing@gmail.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("linked_google_jwt");

        AuthResponse response = authService.loginWithGoogle(request);

        assertNotNull(response);
        assertEquals("linked_google_jwt", response.getAccessToken());
        assertEquals("GOOGLE", existingUser.getAuthProvider());
        assertEquals("google_12345", existingUser.getProviderId());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testLoginWithGoogle_AdminUserWithoutSocialLink_ThrowsBadRequestException() {
        GoogleLoginRequest request = GoogleLoginRequest.builder().idToken("valid_google_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("google_12345")
                .email("admin@shop.com")
                .fullName("Admin User")
                .provider("GOOGLE")
                .build();

        Role adminRole = Role.builder().idRole(99).nameRole("ADMIN").build();
        User adminUser = User.builder()
                .idUser(1)
                .username("admin@shop.com")
                .email("admin@shop.com")
                .providerId(null)
                .enabled(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        when(googleClient.verifyAndGetProfile("valid_google_token")).thenReturn(profile);
        when(userRepository.findByEmail("admin@shop.com")).thenReturn(Optional.of(adminUser));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.loginWithGoogle(request));
        assertTrue(exception.getMessage().contains("Quản trị viên"));
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void testLoginWithGoogle_ProviderMismatch_ThrowsBadRequestException() {
        GoogleLoginRequest request = GoogleLoginRequest.builder().idToken("valid_google_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("google_new_id")
                .email("bound@gmail.com")
                .provider("GOOGLE")
                .build();

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        User existingUser = User.builder()
                .idUser(103)
                .email("bound@gmail.com")
                .authProvider("FACEBOOK")
                .providerId("facebook_different_id")
                .enabled(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        when(googleClient.verifyAndGetProfile("valid_google_token")).thenReturn(profile);
        when(userRepository.findByEmail("bound@gmail.com")).thenReturn(Optional.of(existingUser));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.loginWithGoogle(request));
        assertTrue(exception.getMessage().contains("đã được liên kết với một tài khoản"));
    }

    @Test
    void testLoginWithFacebook_Success_ReturnsJwt() {
        FacebookLoginRequest request = FacebookLoginRequest.builder().accessToken("valid_fb_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("fb_98765")
                .email("fbuser@facebook.com")
                .fullName("Facebook User")
                .avatarUrl("https://graph.facebook.com/avatar.jpg")
                .provider("FACEBOOK")
                .build();

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        when(facebookClient.verifyAndGetProfile("valid_fb_token")).thenReturn(profile);
        when(userRepository.findByEmail("fbuser@facebook.com")).thenReturn(Optional.empty());
        when(roleRepository.findByNameRole("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_uuid_password");

        User savedUser = User.builder()
                .idUser(104)
                .username("fbuser@facebook.com")
                .email("fbuser@facebook.com")
                .fullName("Facebook User")
                .avatar("https://graph.facebook.com/avatar.jpg")
                .authProvider("FACEBOOK")
                .providerId("fb_98765")
                .enabled(true)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("fb_jwt_token_123");

        AuthResponse response = authService.loginWithFacebook(request);

        assertNotNull(response);
        assertEquals("fb_jwt_token_123", response.getAccessToken());
        assertEquals("fbuser@facebook.com", response.getEmail());
    }

    @Test
    void testLoginWithFacebook_DisabledUser_ThrowsBadRequestException() {
        FacebookLoginRequest request = FacebookLoginRequest.builder().accessToken("valid_fb_token").build();
        OAuthProfile profile = OAuthProfile.builder()
                .providerId("fb_98765")
                .email("disabled@facebook.com")
                .provider("FACEBOOK")
                .build();

        Role customerRole = Role.builder().idRole(1).nameRole("CUSTOMER").build();
        User disabledUser = User.builder()
                .idUser(105)
                .email("disabled@facebook.com")
                .enabled(false)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        when(facebookClient.verifyAndGetProfile("valid_fb_token")).thenReturn(profile);
        when(userRepository.findByEmail("disabled@facebook.com")).thenReturn(Optional.of(disabledUser));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.loginWithFacebook(request));
        assertTrue(exception.getMessage().contains("vô hiệu hóa"));
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
