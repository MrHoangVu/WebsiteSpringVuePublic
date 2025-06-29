// src/main/java/com/example/demo/dto/ProductDetailDTO.java
package com.example.demo.dto.product;

import com.example.demo.dto.category.CategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String dimensions;
    private String material;
    private CategoryDTO category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive; // <<< ĐẢM BẢO TRƯỜNG NÀY TỒN TẠI VÀ ĐÚNG KIỂU Boolean
}