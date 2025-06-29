package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "Articles", indexes = { // <<< THÊM INDEXES
        @Index(name = "idx_article_slug", columnList = "slug", unique = true),
        @Index(name = "idx_article_published_status", columnList = "is_published, published_at")
})
@Getter
@Setter
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // FK đến Users
    private User user; // Tác giả bài viết

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 300)
    private String slug;

    @Lob // Cho NVARCHAR(MAX) hoặc TEXT
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "excerpt", length = 500) // Tóm tắt
    private String excerpt;

    @Column(name = "featured_image_url", length = 512) // URL ảnh đại diện
    private String featuredImageUrl;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false; // Mặc định là chưa publish

    @Column(name = "published_at") // Thời điểm publish
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}