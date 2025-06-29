package com.example.demo.controller.admin;

import com.example.demo.dto.article.ArticleDetailDTO;
import com.example.demo.dto.article.ArticleSummaryDTO;
import com.example.demo.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles") // Endpoint chung cho articles
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // API Endpoint: GET /api/articles
    // Lấy danh sách bài viết đã publish (phân trang)
    @GetMapping
    public ResponseEntity<Page<ArticleSummaryDTO>> getPublishedArticles(
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ArticleSummaryDTO> articlePage = articleService.getPublishedArticles(pageable);
        return ResponseEntity.ok(articlePage);
    }

    // API Endpoint: GET /api/articles/{slug}
    // Lấy chi tiết bài viết đã publish theo slug
    @GetMapping("/{slug}")
    public ResponseEntity<ArticleDetailDTO> getPublishedArticleBySlug(@PathVariable String slug) {
        ArticleDetailDTO article = articleService.getPublishedArticleBySlug(slug);
        return ResponseEntity.ok(article);
    }
}