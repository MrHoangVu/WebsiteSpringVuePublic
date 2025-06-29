// src/main/java/com/example/demo/repository/ProductRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< Đã có import này
import org.springframework.data.jpa.repository.Query; // <<< Import này vẫn cần nếu giữ lại @Query
import org.springframework.data.repository.query.Param; // <<< Import này vẫn cần nếu giữ lại @Query
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
//                                                                  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
//                                                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // Kế thừa JpaSpecificationExecutor cho phép dùng Specification API

    // Các phương thức truy vấn cụ thể
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugAndIsActiveTrue(String slug);

    // Các phương thức này CÓ THỂ được thay thế bằng Specification nếu muốn
    // Page<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId, Pageable pageable); // Có thể bỏ nếu dùng spec

    // Phương thức search cũ (dùng cho trang user) - CÓ THỂ được thay thế bằng Specification
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(concat('%', :keyword, '%')) OR p.description LIKE concat('%', :keyword, '%'))")
    Page<Product> searchActiveByNameOrDescriptionContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable); // Có thể bỏ nếu dùng spec

    // Không cần định nghĩa các phương thức tìm kiếm phức tạp cho admin ở đây nữa
    // vì chúng ta sẽ dùng Specification để xây dựng query động trong tầng Service.
}