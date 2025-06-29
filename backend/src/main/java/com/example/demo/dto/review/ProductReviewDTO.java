package com.example.demo.dto.review;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductReviewDTO {
    private Long id;
    private Integer productId; // Chỉ cần ID sản phẩm ở đây
    private String username; // Hiển thị tên người dùng, không phải ID
    private String userFullName; // Có thể thêm tên đầy đủ nếu muốn
    private Byte rating;
    private String comment;
    private LocalDateTime createdAt;
    // Không cần isApproved vì chỉ lấy review đã duyệt cho client
}