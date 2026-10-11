package com.hoangbaoshop.hoangbaocosmetics_backend.dto.common;

import lombok.*;

import java.time.LocalDateTime;

//mẫu chung response
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    //mẫu response nếu thành công request được
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    //trả lời nhanh "thành công" khoi truyển message
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Thành công");
    }

    //tạo response nếu thất bại
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
