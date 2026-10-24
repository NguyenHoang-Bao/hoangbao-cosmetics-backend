package com.hoangbaoshop.hoangbaocosmetics_backend.service;

// 7. Java Standard Libraries
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// 1. Tầng Client & DTO nội bộ
import com.hoangbaoshop.hoangbaocosmetics_backend.client.FacebookClient;
import com.hoangbaoshop.hoangbaocosmetics_backend.client.GoogleClient;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.oauth.OAuthProfile;
// 2. DTO Request & Response
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.ResetPasswordWithOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.SendOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.VerifyOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
// 3. Entity & Enum
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
// 4. Repository & Exception
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
// 5. Spring Security & JWT
import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.JwtUtils;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsImpl;

// 6. Spring Framework & Core Annotations
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final FacebookClient facebookClient;
    private final GoogleClient googleClient;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    // đăng ký tài khoản mới
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // Chuẩn hóa email nếu có
        String cleanEmail = (request.getEmail() != null && !request.getEmail().isBlank())
                ? request.getEmail().trim().toLowerCase()
                : null;

        // kiểm tra tài khoản tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại!");
        }
        if (cleanEmail != null && userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("Email đã được sử dụng!");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại đã được sử dụng!");
        }

        // tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(cleanEmail)
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .build();

        // mặc định đăng ký tài khoản là customer
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByNameRole("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy Role CUSTOMER trong Database."));
        roles.add(userRole);
        user.setRoles(roles);

        // save database
        User savedUser = userRepository.save(user);

        // gửi email xác thực tài khoản 1-click nếu có email (sau khi transaction commit
        // thành công)
        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            String token = UUID.randomUUID().toString().replace("-", "");
            OtpToken verificationToken = OtpToken.builder()
                    .email(savedUser.getEmail())
                    .otpCode(token)
                    .tokenType(OtpTokenType.REGISTER_VERIFY)
                    .expiryTime(LocalDateTime.now().plusHours(24))
                    .isUsed(false)
                    .attemptCount(0)
                    .build();
            otpTokenRepository.save(verificationToken);

            String verificationUrl = backendUrl + "/api/auth/verify-email?token=" + token;
            String userEmail = savedUser.getEmail();
            String fullName = savedUser.getFullName();
            executeAfterCommit(() -> emailService.sendAccountVerificationEmail(userEmail, fullName, verificationUrl));
        }
    }

    // đăng nhập bằng username / password
    @Override
    public AuthResponse login(LoginRequest request) {
        // Xác thực tài khoản qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        // Đặt thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh ra chuỗi JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Lấy thông tin user hiện tại
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Trả về Response cho Frontend
        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .idUser(userDetails.getIdUser())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .avatar(userDetails.getAvatar())
                .roles(roles)
                .build();
    }

    // đăng nhập bằng Google OAuth
    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        OAuthProfile profile = googleClient.verifyAndGetProfile(request.getIdToken());
        return processOAuthLogin(profile);
    }

    // đăng nhập bằng Facebook OAuth
    @Override
    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        OAuthProfile profile = facebookClient.verifyAndGetProfile(request.getAccessToken());
        return processOAuthLogin(profile);
    }

    // ==================== PIPELINE DÙNG CHUNG CHO MỌI NHÀ CUNG CẤP OAUTH
    // ====================
    private AuthResponse processOAuthLogin(OAuthProfile profile) {
        String email = profile.email();
        String provider = profile.provider();
        String providerId = profile.providerId();
        String fullName = profile.fullName();
        String avatarUrl = profile.avatarUrl();

        // Cắt ngắn avatar nếu vượt quá độ dài database cho phép (255 ký tự)
        String safeAvatar = (avatarUrl != null && avatarUrl.length() > 255)
                ? avatarUrl.substring(0, 255)
                : avatarUrl;

        // 1. Tìm tài khoản trong database theo Email
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role customerRole = roleRepository.findByNameRole("CUSTOMER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER không tồn tại trong hệ thống."));

            // Tạo người dùng mới với Role CUSTOMER
            User newUser = User.builder()
                    .username(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .fullName(fullName != null ? fullName : "Khách hàng " + provider)
                    .email(email)
                    .avatar(safeAvatar)
                    .authProvider(provider)
                    .providerId(providerId)
                    .enabled(true)
                    .emailVerified(true)
                    .roles(new HashSet<>(Set.of(customerRole)))
                    .build();

            return userRepository.save(newUser);
        });

        // 2. Kiểm tra nếu tài khoản đã bị vô hiệu hóa
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên!");
        }

        // 3. BẢO MẬT: Chặn chiếm đoạt tài khoản Admin qua mạng xã hội
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNameRole())
                        || "ROLE_ADMIN".equalsIgnoreCase(r.getNameRole()));
        if (isAdmin && user.getProviderId() == null) {
            log.warn("CẢNH BÁO BẢO MẬT: Từ chối liên kết mạng xã hội {} với tài khoản Quản trị viên: {}", provider,
                    email);
            throw new BadRequestException(
                    "Tài khoản Quản trị viên không được phép đăng nhập qua mạng xã hội. Vui lòng đăng nhập bằng mật khẩu!");
        }

        // 4. BẢO MẬT: Chặn tranh chấp Provider (nếu đã liên kết với bên khác)
        if (user.getProviderId() != null && !providerId.equals(user.getProviderId())) {
            throw new BadRequestException(
                    "Email này đã được liên kết với một tài khoản " + user.getAuthProvider() + " khác!");
        }

        // 5. Nếu là tài khoản đăng ký thường trước đó thì liên kết thêm Provider ID
        if (user.getProviderId() == null) {
            user.setAuthProvider(provider);
            user.setProviderId(providerId);
            if (user.getAvatar() == null && safeAvatar != null) {
                user.setAvatar(safeAvatar);
            }
            userRepository.save(user);
        }

        // 6. Nạp thông tin vào Spring Security Context
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. Sinh chuỗi JWT Token và trả về AuthResponse
        String jwt = jwtUtils.generateJwtToken(authentication);
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .idUser(userDetails.getIdUser())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .avatar(userDetails.getAvatar())
                .roles(roles)
                .build();
    }

    // gửi mã OTP đặt lại mật khẩu qua email
    @Override
    @Transactional
    public void sendForgotPasswordOtp(SendOtpRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản với email: " + request.getEmail()));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("Tài khoản này đã bị khóa. Vui lòng liên hệ ban quản trị!");
        }

        otpService.generateAndSendOtp(cleanEmail, OtpTokenType.FORGOT_PASSWORD);
    }

    // xác thực mã OTP đặt lại mật khẩu
    @Override
    @Transactional
    public void verifyForgotPasswordOtp(VerifyOtpRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (!userRepository.existsByEmail(cleanEmail)) {
            throw new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail());
        }

        otpService.verifyOtp(cleanEmail, request.getOtpCode(), OtpTokenType.FORGOT_PASSWORD);
    }

    // đặt lại mật khẩu mới bằng mã OTP
    @Override
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordWithOtpRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản với email: " + request.getEmail()));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("Tài khoản này đã bị khóa!");
        }

        // Kiểm tra và tiêu thụ OTP (đánh dấu đã sử dụng)
        otpService.consumeOtp(cleanEmail, request.getOtpCode(), OtpTokenType.FORGOT_PASSWORD);

        // Cập nhật mật khẩu mã hóa mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // xác thực tài khoản qua liên kết 1-click gửi trong email
    @Override
    @Transactional
    public void verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Mã xác thực không hợp lệ!");
        }

        OtpToken otpToken = otpTokenRepository
                .findTopByOtpCodeAndTokenTypeAndIsUsedFalse(token.trim(), OtpTokenType.REGISTER_VERIFY)
                .orElseThrow(() -> new BadRequestException("Liên kết xác thực không hợp lệ hoặc đã được sử dụng!"));

        if (otpToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "Liên kết xác thực đã hết hạn! Vui lòng liên hệ ban quản trị để được hỗ trợ.");
        }

        otpToken.setIsUsed(true);
        otpTokenRepository.save(otpToken);

        User user = userRepository.findByEmail(otpToken.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản người dùng tương ứng!"));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Xác thực email thành công cho tài khoản: {}", user.getEmail());
    }

    // thực thi tác vụ sau khi database transaction đã commit thành công
    private void executeAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            if (jwtUtils.validateJwtToken(token)) {
                java.util.Date expiration = jwtUtils.getExpirationDateFromJwtToken(token);
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0 && redisTemplate != null) {
                    redisTemplate.opsForValue().set("blacklist:jwt:" + token, "LOGGED_OUT",
                            java.time.Duration.ofMillis(remainingMillis));
                    log.info("Token đã được đưa vào blacklist Redis với thời hạn {} ms", remainingMillis);
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi khi đưa token vào blacklist: {}", e.getMessage());
        }
    }
}