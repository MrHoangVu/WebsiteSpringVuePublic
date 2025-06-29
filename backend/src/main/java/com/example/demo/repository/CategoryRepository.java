package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Tìm kiếm danh mục dựa trên slug.
     * @param slug Slug của danh mục cần tìm.
     * @return Optional chứa Category nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Category> findBySlug(String slug);

    Optional<Category> findFirstByNameContainingIgnoreCase(String nameKeyword);
}