package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.feedback.FeedbackRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.feedback.FeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {

    FeedbackResponse createFeedback(FeedbackRequest request);

    Page<FeedbackResponse> getAllFeedbacks(Pageable pageable);

    FeedbackResponse getFeedbackById(Integer id);

    FeedbackResponse markAsRead(Integer id);

    void deleteFeedback(Integer id);
}
