package com.example.demo.dto.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequestDTO {

    // Không cần productId ở đây vì sẽ lấy từ path variable

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Byte rating;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters") // Giới hạn độ dài comment
    private String comment; // Comment có thể không bắt buộc

    // user_id sẽ lấy từ Principal (người dùng đang đăng nhập)
}