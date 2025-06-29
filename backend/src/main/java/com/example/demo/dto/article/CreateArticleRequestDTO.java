package com.example.demo.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 250, message = "Title must be between 5 and 250 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 50, message = "Content must be at least 50 characters") // Độ dài tối thiểu
    private String content;

    @Size(max = 500, message = "Excerpt cannot exceed 500 characters")
    private String excerpt; // Tóm tắt có thể null

    @Size(max = 512, message = "Image URL cannot exceed 512 characters")
    private String featuredImageUrl; // URL ảnh có thể null

    // slug sẽ được tạo tự động ở backend
    // user_id sẽ lấy từ Principal
    // is_published sẽ mặc định là false (cần admin duyệt)
}