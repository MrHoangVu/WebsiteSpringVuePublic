// src/main/java/com/example/demo/service/ProductReviewService.java
package com.example.demo.service;

import com.example.demo.dto.review.CreateReviewRequestDTO;
import com.example.demo.dto.review.ProductReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductReviewService {


    Page<ProductReviewDTO> getApprovedReviewsByProductId(Integer productId, Pageable pageable);
    ProductReviewDTO createReview(Integer productId, CreateReviewRequestDTO request, String username);

}