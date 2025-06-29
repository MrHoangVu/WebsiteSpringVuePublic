// src/main/java/com/example/demo/repository/specification/ArticleSpecifications.java
package com.example.demo.repository.specification;

import com.example.demo.entity.Article;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ArticleSpecifications {

    public static Specification<Article> filterAdminArticles(String keyword, Boolean isPublished) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo Keyword (tìm trong title, content, excerpt)
            if (StringUtils.hasText(keyword)) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordLower);
                // Bỏ lower cho content và excerpt
                Predicate contentLike = criteriaBuilder.like(root.get("content"), "%" + keyword + "%");
                Predicate excerptLike = criteriaBuilder.like(root.get("excerpt"), "%" + keyword + "%");
                predicates.add(criteriaBuilder.or(titleLike, contentLike, excerptLike));
            }

            // Lọc theo trạng thái Published
            if (isPublished != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublished"), isPublished));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}