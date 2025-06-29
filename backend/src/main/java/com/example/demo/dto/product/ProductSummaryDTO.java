package com.example.demo.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private Integer id;
    private String name;
    private String slug;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    // Có thể thêm tên danh mục nếu cần hiển thị ngay trên danh sách
    // private String categoryName;
}