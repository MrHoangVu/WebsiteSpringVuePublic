package com.example.demo.dto.article;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleSummaryDTO {
    private Long id;
    private String title;
    private String slug;
    private String excerpt; // Tóm tắt
    private String featuredImageUrl; // Ảnh đại diện
    private String authorUsername; // Tên người đăng
    private LocalDateTime publishedAt; // Ngày đăng (nếu đã publish)
    private LocalDateTime createdAt; // Hoặc ngày tạo
    private boolean IsPublished; // Trạng thái đã publish hay chưa
}