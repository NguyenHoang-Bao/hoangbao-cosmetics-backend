package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.feedback.FeedbackRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.feedback.FeedbackResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Feedback;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        Feedback feedback = Feedback.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .phone(request.getPhone().trim())
                .subject(request.getSubject().trim())
                .message(request.getMessage().trim())
                .isRead(false)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return mapToResponse(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getAllFeedbacks(Pageable pageable) {
        return feedbackRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Integer id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản hồi với ID: " + id));
        return mapToResponse(feedback);
    }

    @Override
    @Transactional
    public FeedbackResponse markAsRead(Integer id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản hồi với ID: " + id));
        feedback.setIsRead(true);
        return mapToResponse(feedbackRepository.save(feedback));
    }

    @Override
    @Transactional
    public void deleteFeedback(Integer id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phản hồi với ID: " + id));
        feedbackRepository.delete(feedback);
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .idFeedback(feedback.getIdFeedback())
                .name(feedback.getName())
                .email(feedback.getEmail())
                .phone(feedback.getPhone())
                .subject(feedback.getSubject())
                .message(feedback.getMessage())
                .isRead(feedback.getIsRead())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
