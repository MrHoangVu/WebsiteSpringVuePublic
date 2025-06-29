package com.example.demo.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer id;
    private String name;
    private String slug;
    // Không cần description hoặc danh sách products ở đây cho API danh sách danh mục
}