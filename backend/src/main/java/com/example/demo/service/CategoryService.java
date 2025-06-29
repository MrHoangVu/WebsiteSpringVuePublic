// src/main/java/com/example/demo/service/CategoryService.java
package com.example.demo.service;

import com.example.demo.dto.category.CategoryDTO;

import java.util.List;


public interface CategoryService {

    List<CategoryDTO> findAllCategories();
    CategoryDTO findCategoryBySlug(String slug);

    // Có thể thêm các phương thức khác sau này (tìm theo ID, tạo, sửa, xóa...)

}