package com.hoangbaoshop.hoangbaocosmetics_backend.controller;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.feedback.FeedbackRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.feedback.FeedbackResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // [POST] Gửi tin nhắn liên hệ (Public cho mọi khách hàng)
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> sendFeedback(@Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse response = feedbackService.createFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gửi tin nhắn liên hệ thành công! Hoàng Bảo Cosmetics sẽ phản hồi sớm nhất."));
    }

    // [GET] Danh sách phản hồi cho Quản trị viên (Admin / Staff)
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FeedbackResponse> feedbackPage = feedbackService.getAllFeedbacks(pageable);
        PageResponse<FeedbackResponse> response = PageResponse.<FeedbackResponse>builder()
                .content(feedbackPage.getContent())
                .pageNo(feedbackPage.getNumber())
                .pageSize(feedbackPage.getSize())
                .totalElements(feedbackPage.getTotalElements())
                .totalPages(feedbackPage.getTotalPages())
                .last(feedbackPage.isLast())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [PATCH] Đánh dấu đã đọc phản hồi (Admin / Staff)
    @PatchMapping("/admin/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> markAsRead(@PathVariable Integer id) {
        FeedbackResponse response = feedbackService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã đánh dấu đã đọc phản hồi"));
    }

    // [DELETE] Xóa phản hồi (Admin / Staff)
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Integer id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phản hồi thành công"));
    }
}
