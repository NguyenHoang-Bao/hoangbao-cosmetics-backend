package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.LoginRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.RegisterRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.auth.AuthResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Role;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.User;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.RoleRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.UserRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.jwt.JwtUtils;
import com.hoangbaoshop.hoangbaocosmetics_backend.security.service.UserDetailsImpl;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.GoogleLoginRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collections;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.FacebookLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.SendOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.VerifyOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.auth.ResetPasswordWithOtpRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import java.util.Map;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.OtpTokenRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.OtpToken;
import java.time.LocalDateTime;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

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

        //kiểm tra tài khoản tồn tại chưa
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

        // gửi email xác thực tài khoản 1-click nếu có email (sau khi transaction commit thành công)
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
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Đặt thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Sinh ra chuỗi JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Lấy thông tin user hiện tại
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
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
    @Transactional
    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            String token = request.getIdToken();
            String email = null;
            String googleId = null;
            String fullName = null;
            String pictureUrl = null;

            // 1. Trường hợp Token là JWT ID Token (chứa dấu '.')
            if (token != null && token.contains(".")) {
                try {
                    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            GsonFactory.getDefaultInstance()
                    )
                            .setAudience(Collections.singletonList(googleClientId))
                            .build();

                    GoogleIdToken googleIdToken = verifier.verify(token);
                    if (googleIdToken != null) {
                        GoogleIdToken.Payload payload = googleIdToken.getPayload();
                        email = payload.getEmail();
                        googleId = payload.getSubject();
                        fullName = (String) payload.get("name");
                        pictureUrl = (String) payload.get("picture");
                    }
                } catch (Exception ignored) {
                }
            }

            // 2. Trường hợp Token là Access Token (Xác thực Audience qua tokeninfo chống Confused Deputy Attack)
            if (email == null && token != null) {
                try {
                    String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
                    RestTemplate restTemplate = new RestTemplate();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tokenInfo = restTemplate.getForObject(tokenInfoUrl, Map.class);
                    if (tokenInfo != null) {
                        String tokenAud = (String) tokenInfo.get("aud");
                        String tokenAzp = (String) tokenInfo.get("azp");

                        // Chặn đứng Confused Deputy Attack: Bắt buộc Audience hoặc Authorized Party phải khớp với Google Client ID của hệ thống
                        boolean isAudienceValid = (googleClientId != null && !googleClientId.isBlank()) &&
                                (googleClientId.equals(tokenAud) || googleClientId.equals(tokenAzp));

                        if (!isAudienceValid) {
                            log.warn("Google Access Token bị từ chối: Token Audience '{}' / AZP '{}' không khớp với Client ID '{}'!",
                                    tokenAud, tokenAzp, googleClientId);
                            throw new BadRequestException("Google Token không thuộc về ứng dụng này!");
                        }

                        // Sau khi xác thực Audience an toàn, lấy thông tin chi tiết người dùng
                        String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + token;
                        @SuppressWarnings("unchecked")
                        Map<String, Object> googleResponse = restTemplate.getForObject(googleUserInfoUrl, Map.class);
                        if (googleResponse != null && googleResponse.get("sub") != null) {
                            googleId = (String) googleResponse.get("sub");
                            email = (String) googleResponse.get("email");
                            fullName = (String) googleResponse.get("name");
                            pictureUrl = (String) googleResponse.get("picture");
                        }
                    }
                } catch (BadRequestException bre) {
                    throw bre;
                } catch (Exception ex) {
                    log.error("Lỗi khi kiểm tra và xác thực Google Access Token: {}", ex.getMessage());
                }
            }

            if (email == null) {
                throw new BadRequestException("Google Token không hợp lệ hoặc đã hết hạn!");
            }

            final String finalEmail = email;
            final String finalGoogleId = googleId;
            final String finalFullName = fullName;
            final String finalPictureUrl = pictureUrl;

            // 3. Tìm trong database, nếu chưa có thì tự động tạo tài khoản mới
            User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
                Role customerRole = roleRepository.findByNameRole("CUSTOMER")
                        .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER không tồn tại trong hệ thống."));

                // Cắt ngắn avatar nếu vượt quá độ dài database cho phép
                String safeAvatar = (finalPictureUrl != null && finalPictureUrl.length() > 255) 
                        ? finalPictureUrl.substring(0, 255) : finalPictureUrl;

                // Tạo user mới với quyền CUSTOMER
                User newUser = User.builder()
                        .username(finalEmail) // Mặc định lấy email làm username
                        .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Mật khẩu ngẫu nhiên an toàn
                        .fullName(finalFullName != null ? finalFullName : "Khách hàng")
                        .email(finalEmail)
                        .avatar(safeAvatar)
                        .authProvider("GOOGLE")
                        .providerId(finalGoogleId)
                        .enabled(true)
                        .emailVerified(true)
                        .roles(new HashSet<>(Set.of(customerRole)))
                        .build();

                return userRepository.save(newUser);
            });

            // 4. Kiểm tra nếu tài khoản đã bị Admin khóa
            if (Boolean.FALSE.equals(user.getEnabled())) {
                throw new BadRequestException("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên!");
            }

            // 5. Nếu tài khoản đã có sẵn (đăng ký thường trước đó) thì liên kết thêm Google ID
            if (user.getProviderId() == null) {
                user.setAuthProvider("GOOGLE");
                user.setProviderId(finalGoogleId);
                if (user.getAvatar() == null && finalPictureUrl != null) {
                    String safeAvatar = finalPictureUrl.length() > 255 ? finalPictureUrl.substring(0, 255) : finalPictureUrl;
                    user.setAvatar(safeAvatar);
                }
                userRepository.save(user);
            }

            // 5. Nạp thông tin vào Spring Security Context
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 6. Sinh chuỗi JWT Token của riêng website bạn
            String jwt = jwtUtils.generateJwtToken(authentication);
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // 7. Trả về AuthResponse hoàn chỉnh cho Frontend
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

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Xác thực tài khoản Google thất bại: " + e.getMessage());
        }
    }

    // đăng nhập bằng Facebook OAuth
    @Override
    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        try {
            // 1. Gọi sang Facebook Graph API để xác minh token và lấy thông tin tài khoản
            String fbGraphUrl = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token=" + request.getAccessToken();
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> fbResponse = restTemplate.getForObject(fbGraphUrl, Map.class);

            if (fbResponse == null || fbResponse.get("id") == null) {
                throw new BadRequestException("Facebook Access Token không hợp lệ hoặc đã hết hạn!");
            }

            String fbId = (String) fbResponse.get("id");
            String fullName = (String) fbResponse.get("name");
            String email = (String) fbResponse.get("email");

            // Xử lý tài khoản Facebook đăng ký bằng SĐT không có email
            if (email == null || email.isBlank()) {
                email = fbId + "@facebook.com";
            }

            // Lấy link ảnh đại diện Facebook
            String pictureUrl = null;
            if (fbResponse.get("picture") instanceof Map<?, ?> pictureMap) {
                if (pictureMap.get("data") instanceof Map<?, ?> dataMap) {
                    pictureUrl = (String) dataMap.get("url");
                }
            }

            // 2. Tìm hoặc Tạo mới User trong MySQL
            final String finalEmail = email;
            final String safeAvatar = (pictureUrl != null && pictureUrl.length() > 255) 
                    ? pictureUrl.substring(0, 255) : pictureUrl;
            
            User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
                Role customerRole = roleRepository.findByNameRole("CUSTOMER")
                        .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER không tồn tại trong hệ thống."));

                User newUser = User.builder()
                        .username(finalEmail)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .fullName(fullName != null ? fullName : "Khách hàng Facebook")
                        .email(finalEmail)
                        .avatar(safeAvatar)
                        .authProvider("FACEBOOK")
                        .providerId(fbId)
                        .enabled(true)
                        .roles(new HashSet<>(Set.of(customerRole)))
                        .build();

                return userRepository.save(newUser);
            });

            // 3. Kiểm tra nếu tài khoản đã bị Admin khóa
            if (Boolean.FALSE.equals(user.getEnabled())) {
                throw new BadRequestException("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên!");
            }

            // 4. Nếu user đã tồn tại từ trước thì liên kết thêm Facebook ID
            if (user.getProviderId() == null) {
                user.setAuthProvider("FACEBOOK");
                user.setProviderId(fbId);
                if (user.getAvatar() == null && safeAvatar != null) {
                    user.setAvatar(safeAvatar);
                }
                userRepository.save(user);
            }

            // 3. Đưa thông tin vào Security Context và sinh JWT Token
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // 4. Trả về AuthResponse cho Frontend
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

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Đăng nhập bằng Facebook thất bại: " + e.getMessage());
        }
    }

    // gửi mã OTP đặt lại mật khẩu qua email
    @Override
    @Transactional
    public void sendForgotPasswordOtp(SendOtpRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

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

        OtpToken otpToken = otpTokenRepository.findTopByOtpCodeAndTokenTypeAndIsUsedFalse(token.trim(), OtpTokenType.REGISTER_VERIFY)
                .orElseThrow(() -> new BadRequestException("Liên kết xác thực không hợp lệ hoặc đã được sử dụng!"));

        if (otpToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Liên kết xác thực đã hết hạn! Vui lòng liên hệ ban quản trị để được hỗ trợ.");
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
                    redisTemplate.opsForValue().set("blacklist:jwt:" + token, "LOGGED_OUT", java.time.Duration.ofMillis(remainingMillis));
                    log.info("Token đã được đưa vào blacklist Redis với thời hạn {} ms", remainingMillis);
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi khi đưa token vào blacklist: {}", e.getMessage());
        }
    }
}
