package com.hoangbaoshop.hoangbaocosmetics_backend.exception;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleValidationExceptions() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "username", "Tên đăng nhập không được để trống"));
        bindingResult.addError(new FieldError("testObject", "email", "Email không đúng định dạng"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("dummyMethod", String.class), 0
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Tên đăng nhập không được để trống", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Tên đăng nhập không được để trống", response.getBody().getData().get("username"));
        assertEquals("Email không đúng định dạng", response.getBody().getData().get("email"));
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Tên đăng nhập hoặc mật khẩu không chính xác!", response.getBody().getMessage());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Bạn không có quyền thực hiện thao tác này!", response.getBody().getMessage());
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException ex = new BadRequestException("Email đã được sử dụng!");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadRequestException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email đã được sử dụng!", response.getBody().getMessage());
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Không tìm thấy sản phẩm!");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Không tìm thấy sản phẩm!", response.getBody().getMessage());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau!", response.getBody().getMessage());
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String param) {
    }
}
