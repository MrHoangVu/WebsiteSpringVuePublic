package com.example.demo.dto.admin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserListDTO {
    private Integer id;
    private String username;
    private String fullName;
    // private String email; // Thêm nếu có
    private LocalDateTime createdAt;
    private String tier;
    private BigDecimal totalSpent;
    private Boolean isActive;
    private String role; // Thêm role để chắc chắn chỉ lấy CUSTOMER nếu cần lọc
}