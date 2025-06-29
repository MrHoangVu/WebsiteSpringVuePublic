// src/main/java/com/example/demo/exception/UnauthorizedAccessException.java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được ném ra khi người dùng cố gắng truy cập tài nguyên
 * mà họ không có quyền (khác với lỗi 401 Unauthorized - chưa xác thực).
 * Thường tương ứng với HTTP Status Code 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN) // Map exception này với HTTP 403
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}