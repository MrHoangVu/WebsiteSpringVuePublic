// src/main/java/com/example/demo/controller/ProductController.java
package com.example.demo.controller.user;

import com.example.demo.dto.product.ProductDetailDTO;
import com.example.demo.dto.product.ProductSummaryDTO;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort nếu dùng PageableDefault
import org.springframework.data.web.PageableDefault; // Import PageableDefault
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller xử lý các yêu cầu liên quan đến sản phẩm từ phía người dùng (công khai).
 */
@RestController
@RequestMapping("/api/products") // Base path cho các API sản phẩm công khai
@RequiredArgsConstructor // Tự động inject ProductService qua constructor
public class ProductController {

    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    /**
     * Lấy danh sách sản phẩm công khai (chỉ active), hỗ trợ tìm kiếm, lọc và phân trang.
     *
     * @param keyword     Từ khóa tìm kiếm (trong tên, mô tả, chất liệu).
     * @param categoryId  Lọc theo ID danh mục cụ thể (thường dùng khi click từ link slug).
     * @param categoryIds Danh sách ID danh mục để lọc (từ bộ lọc checkboxes).
     * @param minPrice    Giá tối thiểu.
     * @param maxPrice    Giá tối đa.
     * @param material    Chất liệu cần lọc.
     * @param pageable    Thông tin phân trang và sắp xếp (ví dụ: ?page=0&size=12&sort=price,asc).
     * @return ResponseEntity chứa Page<ProductSummaryDTO>.
     */
    @GetMapping
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId, // Dùng khi lọc theo slug
            @RequestParam(required = false) List<Integer> categoryIds, // Dùng khi lọc từ checkboxes
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String material,
            // Cung cấp giá trị mặc định cho phân trang và sắp xếp
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        logger.debug("GET /api/products - Received filters: keyword={}, categoryId={}, categoryIds={}, minPrice={}, maxPrice={}, material={}, pageable={}",
                keyword, categoryId, categoryIds, minPrice, maxPrice, material, pageable);

        // Gọi hàm service đã được cập nhật để xử lý logic lọc và tìm kiếm
        Page<ProductSummaryDTO> productPage = productService.findUserProducts(
                keyword, categoryId, categoryIds, minPrice, maxPrice, material, pageable
        );

        // Trả về kết quả với status 200 OK
        return ResponseEntity.ok(productPage);
    }

    /**
     * Lấy chi tiết một sản phẩm công khai (chỉ active) dựa trên slug.
     *
     * @param slug Slug của sản phẩm.
     * @return ResponseEntity chứa ProductDetailDTO.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu không tìm thấy sản phẩm active với slug đó.
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ProductDetailDTO> getProductBySlug(@PathVariable String slug) {
        logger.debug("GET /api/products/{}", slug);
        // Service sẽ xử lý việc tìm sản phẩm active hoặc ném lỗi 404
        ProductDetailDTO product = productService.findProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    // Các endpoint khác liên quan đến sản phẩm công khai có thể thêm ở đây (ví dụ: lấy sản phẩm liên quan)
}