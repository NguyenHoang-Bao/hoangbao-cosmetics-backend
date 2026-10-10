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
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra dữ liệu đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại!");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng!");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại đã được sử dụng!");
        }

        // 2. Tạo đối tượng User mới và mã hóa mật khẩu
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .build();

        // 3. Gán Role mặc định là CUSTOMER cho người dùng mới đăng ký
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByNameRole("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy Role CUSTOMER trong Database."));
        roles.add(userRole);
        user.setRoles(roles);

        // 4. Lưu vào Database
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Xác thực tài khoản qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Đặt thông tin xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Sinh ra chuỗi JWT Token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. Lấy thông tin user hiện tại
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        // 5. Trả về Response cho Frontend
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
}