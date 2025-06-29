// src/main/java/com/example/demo/dto/PromotionDTO.java
package com.example.demo.dto.promotion;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer currentUsage;
    private BigDecimal minOrderValue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String targetTiers; // <<< THÊM TRƯỜNG NÀY
}