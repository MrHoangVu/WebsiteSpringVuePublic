// src/main/java/com/example/demo/service/impl/ProductServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.dto.product.ProductCreateUpdateDTO;
import com.example.demo.dto.product.ProductDetailDTO;
import com.example.demo.dto.product.ProductSummaryDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.specification.ProductSpecifications; // Import Specifications
import com.example.demo.service.ProductService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service // Đánh dấu là Service Bean
@RequiredArgsConstructor // Tự động inject dependencies
@Transactional // Mặc định transaction cho các public method
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // Inject Category Repo
    private static final Slugify slugify = Slugify.builder().build(); // Khởi tạo Slugify

    // --- User-facing methods ---

    /**
     * Tìm kiếm và lọc sản phẩm cho trang người dùng (chỉ sản phẩm active).
     * Sử dụng JPA Specifications để xây dựng query động.
     */
    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public Page<ProductSummaryDTO> findUserProducts(
            String keyword, Integer categoryId, List<Integer> categoryIds,
            BigDecimal minPrice, BigDecimal maxPrice, String material,
            Pageable pageable) {

        logger.debug("Finding user products with filters - Keyword: '{}', CategoryId: {}, CategoryIds: {}, MinPrice: {}, MaxPrice: {}, Material: {}, Pageable: {}",
                keyword, categoryId, categoryIds, minPrice, maxPrice, material, pageable);

        // Tạo Specification sử dụng hàm filterUserProducts đã định nghĩa
        // Hàm này tự động thêm điều kiện isActive = true
        Specification<Product> spec = ProductSpecifications.filterUserProducts(
                keyword, categoryId, categoryIds, minPrice, maxPrice, material
        );

        // Gọi repository với Specification và Pageable
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        logger.debug("Found {} user products matching criteria.", productPage.getTotalElements());

        // Map kết quả sang DTO tóm tắt (ProductSummaryDTO)
        return productPage.map(this::mapToProductSummaryDTO);
    }

    /**
     * Lấy chi tiết sản phẩm active theo slug.
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO findProductBySlug(String slug) {
        logger.debug("Finding active product by slug: {}", slug);
        Product product = productRepository.findBySlugAndIsActiveTrue(slug) // Tìm theo slug VÀ isActive = true
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug)); // Ném lỗi 404 nếu không thấy
        // Map sang DTO chi tiết
        return mapToProductDetailDTO(product);
    }

    // --- ADMIN METHODS ---

    /**
     * Lấy danh sách sản phẩm cho admin với bộ lọc (có thể lấy cả inactive).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDetailDTO> findAdminProducts(
            String keyword, Integer categoryId, Boolean isActive, Pageable pageable) {
        logger.info("ADMIN: Fetching products with filters - Keyword: '{}', CategoryId: {}, IsActive: {}, Pageable: {}",
                keyword, categoryId, isActive, pageable);
        // Tạo Specification sử dụng hàm filterAdminProducts
        Specification<Product> spec = ProductSpecifications.filterAdminProducts(keyword, categoryId, isActive);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        logger.info("ADMIN: Found {} products matching criteria.", productPage.getTotalElements());
        // Map sang DTO chi tiết
        return productPage.map(this::mapToProductDetailDTO);
    }

    /**
     * Tạo sản phẩm mới.
     */
    @Override
    @Transactional
    public ProductDetailDTO createProduct(ProductCreateUpdateDTO dto) {
        logger.info("ADMIN: Creating new product with name: {}", dto.getName());
        // 1. Kiểm tra Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        // 2. Tạo slug duy nhất
        String slug = generateUniqueSlug(dto.getName());
        // 3. Tạo Entity
        Product product = new Product();
        BeanUtils.copyProperties(dto, product, "categoryId"); // Copy trừ categoryId
        product.setSlug(slug);
        product.setCategory(category);
        // isActive mặc định là true trong entity hoặc DB
        // 4. Lưu
        Product savedProduct = productRepository.save(product);
        logger.info("ADMIN: Product created successfully with ID: {}", savedProduct.getId());
        // 5. Map và trả về
        return mapToProductDetailDTO(savedProduct);
    }

    /**
     * Cập nhật sản phẩm.
     */
    @Override
    @Transactional
    public ProductDetailDTO updateProduct(Integer productId, ProductCreateUpdateDTO dto) {
        logger.info("ADMIN: Updating product with ID: {}", productId);
        // 1. Tìm Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        // 2. Kiểm tra Category mới (nếu đổi)
        Category category = product.getCategory();
        if (!product.getCategory().getId().equals(dto.getCategoryId())) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        }
        // 3. Cập nhật slug nếu tên đổi (và kiểm tra trùng)
        if (!product.getName().equalsIgnoreCase(dto.getName())) {
            String newSlug = generateUniqueSlug(dto.getName(), productId); // Truyền ID để bỏ qua chính nó khi check
            product.setSlug(newSlug);
        }
        // 4. Copy các trường khác
        BeanUtils.copyProperties(dto, product, "categoryId", "slug"); // Bỏ qua slug và categoryId
        product.setCategory(category);
        // 5. Lưu
        Product updatedProduct = productRepository.save(product);
        logger.info("ADMIN: Product ID {} updated successfully.", productId);
        // 6. Map và trả về
        return mapToProductDetailDTO(updatedProduct);
    }

    /**
     * Lấy chi tiết sản phẩm cho admin (kể cả inactive).
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductByIdForAdmin(Integer productId) {
        logger.info("ADMIN: Fetching product detail (any status) for ID: {}", productId);
        Product product = productRepository.findById(productId) // Tìm bằng ID không cần check active
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToProductDetailDTO(product);
    }

    /**
     * Cập nhật trạng thái active/inactive cho sản phẩm.
     */
    @Override
    @Transactional
    public ProductDetailDTO setProductStatus(Integer productId, boolean isActive) {
        logger.info("ADMIN: Setting status for product ID: {} to {}", productId, isActive ? "Active" : "Inactive");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (product.getIsActive() != isActive) {
            product.setIsActive(isActive);
            Product updatedProduct = productRepository.save(product);
            logger.info("Product ID {} status updated successfully to {}", productId, isActive);
            return mapToProductDetailDTO(updatedProduct);
        } else {
            logger.info("Product ID {} already has status isActive={}. No change needed.", productId, isActive);
            return mapToProductDetailDTO(product);
        }
    }


    // --- Helper Methods ---

    /**
     * Map Product entity sang ProductDetailDTO.
     */
    private ProductDetailDTO mapToProductDetailDTO(Product product) {
        if (product == null) return null;
        ProductDetailDTO dto = new ProductDetailDTO();
        BeanUtils.copyProperties(product, dto, "category"); // Copy trừ category
        dto.setIsActive(product.getIsActive()); // Đảm bảo copy isActive
        if (product.getCategory() != null) {
            dto.setCategory(mapToCategoryDTO(product.getCategory()));
        }
        return dto;
    }

    /**
     * Map Category entity sang CategoryDTO.
     */
    private CategoryDTO mapToCategoryDTO(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    /**
     * Map Product entity sang ProductSummaryDTO (cho trang user).
     */
    private ProductSummaryDTO mapToProductSummaryDTO(Product product) {
        if (product == null) return null;
        ProductSummaryDTO dto = new ProductSummaryDTO();
        // Chỉ copy các trường cần thiết cho summary
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        // dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null); // Thêm nếu cần
        return dto;
    }

    /**
     * Tạo slug duy nhất cho tên sản phẩm.
     * @param name Tên sản phẩm.
     * @return Slug duy nhất.
     */
    private String generateUniqueSlug(String name) {
        return generateUniqueSlug(name, null); // Gọi hàm helper với productId là null
    }

    /**
     * Tạo slug duy nhất, bỏ qua kiểm tra với productId cụ thể (khi update).
     * @param name Tên sản phẩm.
     * @param currentProductId ID sản phẩm hiện tại (null nếu đang tạo mới).
     * @return Slug duy nhất.
     */
    private String generateUniqueSlug(String name, Integer currentProductId) {
        String baseSlug = slugify.slugify(name);
        if (baseSlug.isEmpty()) {
            baseSlug = "san-pham"; // Hoặc tạo slug ngẫu nhiên
        }
        String finalSlug = baseSlug;
        int counter = 1;
        while (true) {
            Optional<Product> existingProduct = productRepository.findBySlug(finalSlug);
            // Nếu không tìm thấy slug hoặc tìm thấy nhưng là của chính sản phẩm đang sửa -> slug hợp lệ
            if (existingProduct.isEmpty() || (currentProductId != null && existingProduct.get().getId().equals(currentProductId))) {
                break; // Thoát vòng lặp
            }
            // Nếu slug đã tồn tại và không phải của sản phẩm đang sửa -> thêm số vào cuối
            finalSlug = baseSlug + "-" + counter++;
            if (counter > 100) { // Ngăn vòng lặp vô hạn
                logger.error("Could not generate unique slug for name '{}' after 100 attempts. Base slug: '{}'", name, baseSlug);
                throw new IllegalStateException("Không thể tạo slug duy nhất cho sản phẩm.");
            }
        }
        logger.trace("Generated slug '{}' for name '{}'", finalSlug, name);
        return finalSlug;
    }
}