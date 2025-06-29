// src/main/java/com/example/demo/service/impl/ArticleServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.article.ArticleCreateUpdateDTO;
import com.example.demo.dto.article.ArticleDetailDTO;
import com.example.demo.dto.article.ArticleSummaryDTO;
import com.example.demo.entity.Article;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.specification.ArticleSpecifications; // Import Specifications
import com.example.demo.service.ArticleService;
import com.github.slugify.Slugify; // Import Slugify
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException; // Import nếu cần bắt lỗi trùng slug
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service // Đánh dấu là Service Bean
@RequiredArgsConstructor // Tự động inject dependencies
@Transactional // Mặc định transaction cho các public method
public class ArticleServiceImpl implements ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    private static final Slugify slugify = Slugify.builder().build(); // Khởi tạo Slugify

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository; // Inject UserRepository

    // --- User Facing ---

    /**
     * Lấy danh sách các bài viết đã publish (phân trang).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> getPublishedArticles(Pageable pageable) {
        logger.info("Fetching published articles with pageable: {}", pageable);
        // Sử dụng phương thức có sẵn của JpaRepository hoặc tạo query riêng nếu cần tối ưu
        Page<Article> articlePage = articleRepository.findByIsPublishedTrue(pageable);
        // Map sang DTO tóm tắt
        return articlePage.map(this::mapToArticleSummaryDTO);
    }

    /**
     * Lấy chi tiết một bài viết đã publish theo slug.
     */
    @Override
    @Transactional(readOnly = true)
    public ArticleDetailDTO getPublishedArticleBySlug(String slug) {
        logger.info("Fetching published article by slug: {}", slug);
        // Tìm bài viết theo slug VÀ isPublished = true
        Article article = articleRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Published Article", "slug", slug));
        // Map sang DTO chi tiết
        return mapToArticleDetailDTO(article);
    }

    // --- Admin Facing ---

    /**
     * Lấy danh sách tất cả bài viết cho admin (cả publish và draft) với bộ lọc.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> getAdminArticles(String keyword, Boolean isPublished, Pageable pageable) {
        logger.info("ADMIN: Fetching articles with filters - Keyword: '{}', IsPublished: {}, Pageable: {}", keyword, isPublished, pageable);
        // Tạo Specification từ các tiêu chí lọc
        Specification<Article> spec = ArticleSpecifications.filterAdminArticles(keyword, isPublished);
        // Gọi repository với Specification và Pageable
        Page<Article> articlePage = articleRepository.findAll(spec, pageable);
        logger.debug("ADMIN: Found {} articles matching criteria.", articlePage.getTotalElements());
        // Map sang DTO tóm tắt (dùng chung DTO summary cho cả admin và user list)
        return articlePage.map(this::mapToArticleSummaryDTO);
    }

    /**
     * Lấy chi tiết bài viết cho admin (bất kể trạng thái).
     */
    @Override
    @Transactional(readOnly = true)
    public ArticleDetailDTO getArticleByIdAdmin(Long id) {
        logger.info("ADMIN: Fetching article details for ID: {}", id);
        // Tìm bài viết theo ID hoặc ném lỗi nếu không thấy
        Article article = findArticleByIdOrThrow(id);
        // Map sang DTO chi tiết
        return mapToArticleDetailDTO(article);
    }

    /**
     * Tạo bài viết mới (mặc định là draft).
     */
    @Override
    @Transactional
    public ArticleDetailDTO createArticle(ArticleCreateUpdateDTO dto, String authorUsername) {
        logger.info("ADMIN: User '{}' creating article with title: {}", authorUsername, dto.getTitle());
        // Tìm tác giả
        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", authorUsername));

        // Tạo slug duy nhất
        String slug = generateUniqueSlug(dto.getTitle());
        // Kiểm tra lại lần nữa (phòng trường hợp race condition nhẹ)
        if (articleRepository.existsBySlug(slug)) {
            throw new DataIntegrityViolationException("Slug '" + slug + "' đã tồn tại. Vui lòng chọn tiêu đề khác.");
        }


        // Tạo entity mới
        Article newArticle = new Article();
        BeanUtils.copyProperties(dto, newArticle); // Copy các trường từ DTO
        newArticle.setUser(author);
        newArticle.setSlug(slug);
        newArticle.setIsPublished(false); // Mặc định chưa publish
        newArticle.setPublishedAt(null);

        // Lưu vào DB
        Article savedArticle = articleRepository.save(newArticle);
        logger.info("ADMIN: Article created successfully with ID: {}", savedArticle.getId());
        // Trả về DTO chi tiết
        return mapToArticleDetailDTO(savedArticle);
    }

    /**
     * Cập nhật bài viết.
     */
    @Override
    @Transactional
    public ArticleDetailDTO updateArticle(Long id, ArticleCreateUpdateDTO dto) {
        logger.info("ADMIN: Updating article with ID: {}", id);
        // Tìm bài viết cần cập nhật
        Article article = findArticleByIdOrThrow(id);

        // Cập nhật các trường từ DTO
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setExcerpt(dto.getExcerpt());
        article.setFeaturedImageUrl(dto.getFeaturedImageUrl());

        // Cập nhật slug nếu tiêu đề thay đổi và kiểm tra trùng lặp (trừ chính nó)
        if (!article.getTitle().equalsIgnoreCase(dto.getTitle())) {
            String newSlug = generateUniqueSlug(dto.getTitle(), id);
            // Kiểm tra lại lần nữa nếu slug thực sự thay đổi
            if (!newSlug.equals(article.getSlug())) {
                if (articleRepository.existsBySlugAndIdNot(newSlug, id)) {
                    throw new DataIntegrityViolationException("Slug '" + newSlug + "' đã tồn tại. Vui lòng chọn tiêu đề khác.");
                }
                article.setSlug(newSlug);
                logger.debug("ADMIN: Regenerated slug for article ID {}: {}", id, newSlug);
            }
        }

        // Lưu thay đổi
        Article updatedArticle = articleRepository.save(article);
        logger.info("ADMIN: Article ID {} updated successfully.", id);
        // Trả về DTO chi tiết
        return mapToArticleDetailDTO(updatedArticle);
    }

    /**
     * Xóa bài viết (xóa cứng).
     */
    @Override
    @Transactional
    public void deleteArticle(Long id) {
        logger.warn("ADMIN: Deleting article with ID: {}", id);
        // Kiểm tra tồn tại trước khi xóa
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article", "id", id);
        }
        articleRepository.deleteById(id); // Xóa cứng khỏi DB
        logger.info("ADMIN: Article ID {} deleted successfully.", id);
    }

    /**
     * Publish một bài viết.
     */
    @Override
    @Transactional
    public ArticleDetailDTO publishArticle(Long id) {
        logger.info("ADMIN: Publishing article with ID: {}", id);
        Article article = findArticleByIdOrThrow(id);
        // Kiểm tra xem đã publish chưa
        if (Boolean.TRUE.equals(article.getIsPublished())) { // Check an toàn với Boolean
            logger.warn("ADMIN: Article ID {} is already published.", id);
            throw new BadRequestException("Bài viết đã được xuất bản rồi.");
        }
        // Đặt trạng thái và thời gian publish
        article.setIsPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        Article publishedArticle = articleRepository.save(article);
        logger.info("ADMIN: Article ID {} published successfully at {}", id, publishedArticle.getPublishedAt());
        // Trả về DTO chi tiết
        return mapToArticleDetailDTO(publishedArticle);
    }

    /**
     * Unpublish (chuyển về draft) một bài viết.
     */
    @Override
    @Transactional
    public ArticleDetailDTO unpublishArticle(Long id) {
        logger.info("ADMIN: Unpublishing article with ID: {}", id);
        Article article = findArticleByIdOrThrow(id);
        // Kiểm tra xem có đang publish không
        if (Boolean.FALSE.equals(article.getIsPublished())) { // Check an toàn với Boolean
            logger.warn("ADMIN: Article ID {} is already unpublished (draft).", id);
            throw new BadRequestException("Bài viết đang ở trạng thái nháp.");
        }
        // Đặt lại trạng thái và xóa thời gian publish
        article.setIsPublished(false);
        article.setPublishedAt(null);
        Article unpublishedArticle = articleRepository.save(article);
        logger.info("ADMIN: Article ID {} unpublished successfully.", id);
        // Trả về DTO chi tiết
        return mapToArticleDetailDTO(unpublishedArticle);
    }

    // --- Helper Methods ---

    /**
     * Helper tìm Article theo ID hoặc ném ResourceNotFoundException.
     */
    private Article findArticleByIdOrThrow(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
    }

    /**
     * Helper map Article entity sang ArticleSummaryDTO.
     */
    private ArticleSummaryDTO mapToArticleSummaryDTO(Article article) {
        if (article == null) return null;
        ArticleSummaryDTO dto = new ArticleSummaryDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSlug(article.getSlug());
        dto.setExcerpt(article.getExcerpt());
        dto.setFeaturedImageUrl(article.getFeaturedImageUrl());
        if (article.getUser() != null) {
            dto.setAuthorUsername(article.getUser().getUsername());
        }
        dto.setPublishedAt(article.getPublishedAt()); // Có thể null nếu là draft
        dto.setCreatedAt(article.getCreatedAt());
        // Thêm isPublished vào DTO summary để admin list biết trạng thái
        dto.setIsPublished(article.getIsPublished()); // <<< THÊM isPublished
        return dto;
    }

    /**
     * Helper map Article entity sang ArticleDetailDTO.
     */
    private ArticleDetailDTO mapToArticleDetailDTO(Article article) {
        if (article == null) return null;
        ArticleDetailDTO dto = new ArticleDetailDTO();
        BeanUtils.copyProperties(article, dto, "user"); // Copy các trường khớp tên, trừ user
        if (article.getUser() != null) {
            dto.setAuthorUsername(article.getUser().getUsername());
            dto.setAuthorFullName(article.getUser().getFullName());
        }
        // Thêm isPublished vào DTO detail để form edit biết trạng thái ban đầu (nếu cần)
        dto.setIsPublished(article.getIsPublished()); // <<< THÊM isPublished
        return dto;
    }

    /**
     * Helper tạo slug duy nhất cho tiêu đề, có kiểm tra trùng lặp.
     * @param title Tiêu đề bài viết.
     * @return Slug duy nhất.
     */
    private String generateUniqueSlug(String title) {
        return generateUniqueSlug(title, null); // Gọi hàm helper với ID là null khi tạo mới
    }

    /**
     * Helper tạo slug duy nhất, bỏ qua kiểm tra trùng với ID hiện tại (khi cập nhật).
     * @param title Tiêu đề bài viết.
     * @param currentArticleId ID của bài viết đang cập nhật (null nếu tạo mới).
     * @return Slug duy nhất.
     */
    private String generateUniqueSlug(String title, Long currentArticleId) {
        String baseSlug = slugify.slugify(title);
        // Xử lý trường hợp slug rỗng (ví dụ: tiêu đề toàn ký tự đặc biệt)
        if (baseSlug.isEmpty()) {
            baseSlug = "bai-viet-" + System.currentTimeMillis(); // Tạo slug dự phòng
            logger.warn("Generated empty base slug for title '{}', using fallback '{}'", title, baseSlug);
        }
        String finalSlug = baseSlug;
        int counter = 1;
        while (true) {
            // Kiểm tra xem slug đã tồn tại chưa
            Optional<Article> existing = articleRepository.findBySlug(finalSlug);
            // Nếu không tồn tại, HOẶC tồn tại nhưng là của chính bài viết đang sửa -> OK
            if (existing.isEmpty() || (currentArticleId != null && existing.get().getId().equals(currentArticleId))) {
                break;
            }
            // Nếu tồn tại và là của bài viết khác -> thêm số vào cuối và thử lại
            finalSlug = baseSlug + "-" + counter++;
            // Ngăn vòng lặp vô hạn
            if (counter > 100) {
                logger.error("Could not generate unique slug for title '{}' after 100 attempts. Base slug: '{}'", title, baseSlug);
                throw new IllegalStateException("Không thể tạo slug duy nhất cho bài viết.");
            }
        }
        logger.trace("Generated final slug '{}' for title '{}'", finalSlug, title);
        return finalSlug;
    }
}