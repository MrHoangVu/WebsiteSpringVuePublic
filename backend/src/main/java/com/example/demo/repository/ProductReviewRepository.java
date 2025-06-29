package com.example.demo.repository;

import com.example.demo.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // Tìm tất cả review của một sản phẩm, có sắp xếp và phân trang
    // Chỉ lấy những review đã được duyệt (isApproved = true)
    Page<ProductReview> findByProductIdAndIsApprovedTrue(Integer productId, Pageable pageable);

    // (Tùy chọn) Kiểm tra xem một user đã review sản phẩm này chưa
    boolean existsByProductIdAndUserId(Integer productId, Integer userId);

    // (Tùy chọn - cho admin) Tìm tất cả review chưa duyệt
    // Page<ProductReview> findByIsApprovedFalse(Pageable pageable);

    // (Tùy chọn) Tìm tất cả review của một sản phẩm (cả duyệt và chưa duyệt - cho admin)
    // Page<ProductReview> findByProductId(Integer productId, Pageable pageable);
}