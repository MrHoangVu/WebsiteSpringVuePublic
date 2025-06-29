// src/main/java/com/example/demo/repository/specification/ProductSpecifications.java
package com.example.demo.repository.specification;

import com.example.demo.entity.Product;
import com.example.demo.entity.Category; // Import cần thiết
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal; // Import cần thiết
import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {

    /**
     * Tạo Specification để lọc sản phẩm dựa trên các tiêu chí từ frontend user.
     * Mặc định chỉ lấy sản phẩm isActive = true.
     */
    public static Specification<Product> filterUserProducts(
            String keyword,
            Integer categoryId,      // Dùng khi click vào link danh mục theo slug
            List<Integer> categoryIds, // Dùng khi lọc bằng checkbox/multiselect
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String material) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // --- LUÔN LỌC SẢN PHẨM ACTIVE CHO USER ---
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
            // -----------------------------------------

            // 1. Keyword Search (Tìm trong name, description, material)
            if (StringUtils.hasText(keyword)) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keywordLower);
                // Bỏ lower() cho description vì là CLOB/NVARCHAR(MAX)
                Predicate descriptionLike = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
                Predicate materialLikeSearch = criteriaBuilder.like(criteriaBuilder.lower(root.get("material")), keywordLower);
                predicates.add(criteriaBuilder.or(nameLike, descriptionLike, materialLikeSearch));
            }

            // 2. Lọc theo Category
            // Ưu tiên categoryId từ slug nếu có
            if (categoryId != null) {
                // Join với Category để lấy id (nếu chưa join tự động)
                // Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER); // Chỉ cần nếu chưa có mapping đúng
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            // Nếu không có categoryId từ slug, thì lọc theo danh sách categoryIds từ filter
            else if (categoryIds != null && !categoryIds.isEmpty()) {
                // Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER); // Chỉ cần nếu chưa có mapping đúng
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // 3. Lọc theo Giá
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                // Đảm bảo maxPrice >= minPrice nếu cả hai cùng tồn tại (nên validate ở frontend/service)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 4. Lọc theo Chất liệu (so sánh chính xác, không phân biệt hoa thường)
            if (StringUtils.hasText(material)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("material")), material.toLowerCase()));
            }

            // Kết hợp tất cả bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tạo Specification để lọc sản phẩm cho trang Admin.
     * Cho phép lọc theo isActive.
     */
    public static Specification<Product> filterAdminProducts(
            String keyword, Integer categoryId, Boolean isActive) { // Đơn giản hơn cho admin list hiện tại

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword Search (Tương tự user hoặc tùy chỉnh)
            if (StringUtils.hasText(keyword)) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keywordLower);
                Predicate slugLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), keywordLower);
                Predicate descriptionLike = criteriaBuilder.like(root.get("description"), "%" + keyword + "%"); // Bỏ lower()
                Predicate materialLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("material")), keywordLower);
                predicates.add(criteriaBuilder.or(nameLike, slugLike, descriptionLike, materialLike));
            }

            // 2. Lọc theo Category ID (Nếu admin cần)
            if (categoryId != null) {
                // Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER); // Chỉ cần nếu chưa có mapping đúng
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // 3. Lọc theo Trạng thái Active (Admin có thể chọn cả 3: true, false, null -> không lọc nếu null)
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            // Kết hợp các điều kiện bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> filterByCriteria(
            Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("isActive"))); // Luôn lấy active

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Tìm keyword trong tên HOẶC mô tả nếu có
            if (StringUtils.hasText(keyword)) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keywordLower);
                // Bỏ lower cho description
                Predicate descriptionLike = criteriaBuilder.like(root.get("description"), "%" + keyword + "%");
                predicates.add(criteriaBuilder.or(nameLike, descriptionLike));
            }

            // Sắp xếp theo giá tăng dần làm mặc định khi tìm theo tiêu chí
            query.orderBy(criteriaBuilder.asc(root.get("price")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}