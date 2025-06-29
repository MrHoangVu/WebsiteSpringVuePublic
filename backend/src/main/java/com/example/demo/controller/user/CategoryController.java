package com.example.demo.controller.user;

import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // Dùng ResponseEntity để kiểm soát response tốt hơn
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories") // Tiền tố chung cho các API trong controller này
@RequiredArgsConstructor // Inject CategoryService
public class CategoryController {

    private final CategoryService categoryService;

    // API Endpoint: GET /api/categories
    // Mục đích: Lấy danh sách tất cả danh mục (cho RQ01)
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories); // Trả về 200 OK cùng với danh sách categories
    }
    // --- ENDPOINT MỚI ---
    // API Endpoint: GET /api/categories/slug/{slug}
    // Mục đích: Lấy thông tin chi tiết một danh mục dựa vào slug của nó.
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {

        CategoryDTO category = categoryService.findCategoryBySlug(slug);
        // Nếu tìm thấy, trả về 200 OK cùng với thông tin category
        return ResponseEntity.ok(category);
    }
    // Các endpoints khác (lấy theo ID, tạo, sửa, xóa) có thể thêm sau
}