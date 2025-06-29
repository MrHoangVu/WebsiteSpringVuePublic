
package com.example.demo.controller.admin;

import com.example.demo.dto.article.ArticleCreateUpdateDTO;
import com.example.demo.dto.article.ArticleDetailDTO;
import com.example.demo.dto.article.ArticleSummaryDTO;
import com.example.demo.service.ArticleService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles") // Base path cho admin articles
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller
public class AdminArticleController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArticleController.class);
    private final ArticleService articleService;

    /**
     * Lấy danh sách tất cả bài viết (cả draft và published) với bộ lọc.
     */
    @GetMapping
    public ResponseEntity<Page<ArticleSummaryDTO>> getAdminArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPublished,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.info("ADMIN GET /api/admin/articles - Filters: keyword={}, isPublished={}, pageable={}", keyword, isPublished, pageable);
        Page<ArticleSummaryDTO> articlePage = articleService.getAdminArticles(keyword, isPublished, pageable);
        return ResponseEntity.ok(articlePage);
    }

    /**
     * Lấy chi tiết một bài viết theo ID (bất kể trạng thái).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailDTO> getArticleById(@PathVariable Long id) {
        logger.info("ADMIN GET /api/admin/articles/{}", id);
        ArticleDetailDTO article = articleService.getArticleByIdAdmin(id);
        return ResponseEntity.ok(article);
    }

    /**
     * Tạo bài viết mới.
     */
    @PostMapping
    public ResponseEntity<ArticleDetailDTO> createArticle(
            @Valid @RequestBody ArticleCreateUpdateDTO dto,
            Authentication authentication) { // Inject Authentication để lấy người tạo
        String username = authentication.getName();
        logger.info("ADMIN POST /api/admin/articles - User '{}' creating article: {}", username, dto.getTitle());
        ArticleDetailDTO createdArticle = articleService.createArticle(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle);
    }

    /**
     * Cập nhật bài viết.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ArticleDetailDTO> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleCreateUpdateDTO dto) {
        logger.info("ADMIN PUT /api/admin/articles/{} - Updating article", id);
        ArticleDetailDTO updatedArticle = articleService.updateArticle(id, dto);
        return ResponseEntity.ok(updatedArticle);
    }

    /**
     * Xóa bài viết.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        logger.warn("ADMIN DELETE /api/admin/articles/{} - Deleting article", id);
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish bài viết.
     */
    @PatchMapping("/{id}/publish") // Dùng PATCH vì chỉ thay đổi trạng thái
    public ResponseEntity<ArticleDetailDTO> publishArticle(@PathVariable Long id) {
        logger.info("ADMIN PATCH /api/admin/articles/{}/publish - Publishing article", id);
        ArticleDetailDTO publishedArticle = articleService.publishArticle(id);
        return ResponseEntity.ok(publishedArticle);
    }

    /**
     * Unpublish bài viết (chuyển về draft).
     */
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ArticleDetailDTO> unpublishArticle(@PathVariable Long id) {
        logger.info("ADMIN PATCH /api/admin/articles/{}/unpublish - Unpublishing article", id);
        ArticleDetailDTO unpublishedArticle = articleService.unpublishArticle(id);
        return ResponseEntity.ok(unpublishedArticle);
    }
}