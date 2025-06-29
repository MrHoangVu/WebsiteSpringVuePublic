// src/main/java/com/example/demo/service/impl/CategoryServiceImpl.java
package com.example.demo.service.impl; // Đặt trong package con "impl"

import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.entity.Category;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService; // Import interface
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Thêm import Logger (nếu muốn log)
import org.slf4j.LoggerFactory; // Thêm import LoggerFactory (nếu muốn log)
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service // Annotation @Service đặt ở đây
@RequiredArgsConstructor // Lombok annotation
@Transactional(readOnly = true) // Transaction mặc định là chỉ đọc
public class CategoryServiceImpl implements CategoryService { // Implement interface

    // Optional: Thêm logger nếu cần
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository; // Dependency Injection

    @Override // Thêm @Override
    public List<CategoryDTO> findAllCategories() {
        logger.debug("Fetching all categories."); // Optional logging
        List<Category> categories = categoryRepository.findAll();
        logger.info("Found {} categories.", categories.size()); // Optional logging
        return categories.stream()
                .map(this::mapToCategoryDTO) // Sử dụng helper method private
                .collect(Collectors.toList());
    }

    @Override // Thêm @Override
    public CategoryDTO findCategoryBySlug(String slug) {
        logger.debug("Fetching category by slug: {}", slug); // Optional logging
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    logger.warn("Category not found with slug: {}", slug); // Optional logging
                    return new ResourceNotFoundException("Danh mục", "slug", slug);
                });
        logger.info("Found category ID: {} for slug: {}", category.getId(), slug); // Optional logging
        return mapToCategoryDTO(category); // Sử dụng helper method private
    }


    // --- Private Helper Method ---
    // Giữ nguyên trong implementation

    /**
     * Chuyển đổi Category entity sang CategoryDTO.
     * @param category Entity Category cần chuyển đổi.
     * @return CategoryDTO tương ứng, hoặc null nếu input là null.
     */
    private CategoryDTO mapToCategoryDTO(Category category) {
        if (category == null) {
            logger.warn("Attempted to map a null Category entity."); // Optional logging
            return null;
        }
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        // Các trường khác của Category không cần thiết cho DTO này
        return dto;
    }
}