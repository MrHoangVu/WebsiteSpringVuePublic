// src/main/java/com/example/demo/service/ArticleService.java
package com.example.demo.service;

import com.example.demo.dto.article.ArticleCreateUpdateDTO;
import com.example.demo.dto.article.ArticleDetailDTO;
import com.example.demo.dto.article.ArticleSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleService {

    // --- User Facing ---
    Page<ArticleSummaryDTO> getPublishedArticles(Pageable pageable);

    ArticleDetailDTO getPublishedArticleBySlug(String slug);

    // --- Admin Facing ---

    /**
     * Lấy danh sách tất cả bài viết cho admin (cả publish và draft) với bộ lọc.
     */
    Page<ArticleSummaryDTO> getAdminArticles(String keyword, Boolean isPublished, Pageable pageable); // <<< MỚI

    /**
     * Lấy chi tiết bài viết cho admin (bất kể trạng thái).
     */
    ArticleDetailDTO getArticleByIdAdmin(Long id); // <<< MỚI

    /**
     * Tạo bài viết mới (mặc định là draft).
     * Cần username của người tạo (lấy từ Principal).
     */
    ArticleDetailDTO createArticle(ArticleCreateUpdateDTO dto, String authorUsername); // <<< Sửa tham số

    /**
     * Cập nhật bài viết.
     */
    ArticleDetailDTO updateArticle(Long id, ArticleCreateUpdateDTO dto); // <<< MỚI

    /**
     * Xóa bài viết.
     */
    void deleteArticle(Long id); // <<< MỚI

    /**
     * Publish một bài viết.
     */
    ArticleDetailDTO publishArticle(Long id); // <<< MỚI

    /**
     * Unpublish (chuyển về draft) một bài viết.
     */
    ArticleDetailDTO unpublishArticle(Long id); // <<< MỚI
}