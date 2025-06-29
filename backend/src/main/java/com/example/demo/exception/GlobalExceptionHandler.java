package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Đánh dấu lớp này xử lý exception toàn cục
public class GlobalExceptionHandler {

    // Bắt cụ thể ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage()); // Lấy message từ exception
        body.put("path", request.getDescription(false).replace("uri=", "")); // Lấy đường dẫn gây lỗi

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Có thể thêm các @ExceptionHandler khác cho các loại Exception khác (ValidationException, AuthenticationException,...)

    // Bắt các Exception chung khác (nếu muốn)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
//        body.put("message", "An unexpected error occurred: " + ex.getMessage()); // Không nên lộ quá chi tiết lỗi trong production
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // Log lỗi chi tiết ở đây cho dev
        System.err.println("Unhandled exception: " + ex); // Thay bằng logger chuẩn
        ex.printStackTrace(); // In stack trace ra console (chỉ nên làm khi dev)


        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}