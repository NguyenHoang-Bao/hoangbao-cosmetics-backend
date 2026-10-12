package com.hoangbaoshop.hoangbaocosmetics_backend.entity;

import com.hoangbaoshop.hoangbaocosmetics_backend.enums.OtpTokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email_type", columnList = "email, token_type"),
        @Index(name = "idx_otp_code", columnList = "otp_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_otp")
    private Long idOtp;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 64)
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private OtpTokenType tokenType;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isUsed == null) {
            this.isUsed = false;
        }
        if (this.attemptCount == null) {
            this.attemptCount = 0;
        }
    }
}
