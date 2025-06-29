package com.example.demo.dto.article;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleDetailDTO {
    private Long id;
    private String title;
    private String slug;
    private String content; // Nội dung đầy đủ
    private String featuredImageUrl;
    private String authorUsername;
    private String authorFullName; // Tên đầy đủ tác giả
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean IsPublished; // Trạng thái đã publish hay chưa
}