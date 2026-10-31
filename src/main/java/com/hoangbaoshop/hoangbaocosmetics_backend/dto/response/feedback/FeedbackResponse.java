package com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.feedback;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private Integer idFeedback;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
