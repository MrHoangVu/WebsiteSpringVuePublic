package com.example.demo.controller.admin;

import com.example.demo.dto.product.ProductCreateUpdateDTO;
import com.example.demo.dto.product.ProductDetailDTO;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class); // Thêm logger

    /**
     * Lấy danh sách tất cả sản phẩm cho admin (kể cả inactive).
     */
    @GetMapping // Endpoint này đã tồn tại, chỉ cần thêm tham số
    public ResponseEntity<Page<ProductDetailDTO>> getAllProducts(
            // <<< THÊM CÁC THAM SỐ LỌC >>>
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isActive,
            // <<< Pageable giữ nguyên hoặc sửa sort mặc định >>>
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        logger.info("GET /api/admin/products - Fetching products with filters: keyword={}, categoryId={}, isActive={}, pageable={}",
                keyword, categoryId, isActive, pageable);

        // Gọi service mới với các tham số lọc
        Page<ProductDetailDTO> productPage = productService.findAdminProducts(keyword, categoryId, isActive, pageable);
        return ResponseEntity.ok(productPage);
    }
    /**
     * Lấy chi tiết một sản phẩm theo ID (kể cả inactive).
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDTO> getProductById(@PathVariable Integer productId) {
        ProductDetailDTO product = productService.getProductByIdForAdmin(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Tạo sản phẩm mới.
     */
    @PostMapping
    public ResponseEntity<ProductDetailDTO> createProduct(
            @Valid @RequestBody ProductCreateUpdateDTO createDto) {
        ProductDetailDTO createdProduct = productService.createProduct(createDto);
        // Trả về 201 Created và thông tin sản phẩm vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Cập nhật thông tin sản phẩm.
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailDTO> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody ProductCreateUpdateDTO updateDto) {
        ProductDetailDTO updatedProduct = productService.updateProduct(productId, updateDto);
        return ResponseEntity.ok(updatedProduct);
    }



    /**
     * Endpoint mới để cập nhật trạng thái active/inactive của sản phẩm.
     * @param productId ID sản phẩm.
     * @param payload Request body dạng JSON, ví dụ: {"isActive": true}
     * @return ProductDetailDTO đã cập nhật.
     */
    @PatchMapping("/{productId}/status") // <<< ENDPOINT MỚI (Dùng PATCH)
    public ResponseEntity<ProductDetailDTO> toggleProductStatus(
            @PathVariable Integer productId,
            @RequestBody Map<String, Boolean> payload) { // <<< Nhận Map đơn giản

        Boolean newStatus = payload.get("isActive"); // Lấy giá trị từ Map
        if (newStatus == null) {
            logger.warn("PATCH /api/admin/products/{}/status - Missing 'isActive' in request body.", productId);
            return ResponseEntity.badRequest().build();
        }

        logger.info("PATCH /api/admin/products/{}/status - Setting isActive to {}", productId, newStatus);
        // Gọi hàm service mới đã tạo
        ProductDetailDTO updatedProduct = productService.setProductStatus(productId, newStatus);
        return ResponseEntity.ok(updatedProduct);
    }

    // Endpoint DELETE cũ có thể được sửa để gọi service mới hoặc bỏ đi
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) {
        logger.warn("DELETE /api/admin/products/{} - Deactivating product via soft delete (use PATCH /status instead for clarity)", productId);
        // Gọi service mới để đảm bảo chỉ cập nhật isActive
        productService.setProductStatus(productId, false); // Set thành inactive
        return ResponseEntity.noContent().build(); // Trả về 204
    }

}