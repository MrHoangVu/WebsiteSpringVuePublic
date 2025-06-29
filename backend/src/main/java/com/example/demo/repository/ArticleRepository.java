// src/main/java/com/example/demo/repository/ArticleRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< THÊM IMPORT
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> { // <<< THÊM KẾ THỪA

    // Tìm bài viết đã publish theo slug (cho user)
    Optional<Article> findBySlugAndIsPublishedTrue(String slug);

    // Tìm tất cả bài viết đã publish (cho user list)
    Page<Article> findByIsPublishedTrue(Pageable pageable);

    // Tìm bài viết theo slug (bất kể trạng thái, cho admin)
    Optional<Article> findBySlug(String slug);

    // Kiểm tra slug tồn tại (trừ ID hiện tại khi update)
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySlug(String slug); // Khi tạo mới
}