// src/main/java/com/example/demo/service/impl/ProductReviewServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.review.CreateReviewRequestDTO;
import com.example.demo.dto.review.ProductReviewDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductReview;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ProductReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductReviewService; // Import interface
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ProductReviewServiceImpl.class);

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewDTO> getApprovedReviewsByProductId(Integer productId, Pageable pageable) {
        logger.info("Fetching approved reviews for product ID: {} with pageable: {}", productId, pageable);
        // Không cần kiểm tra product tồn tại ở đây vì query đã lọc theo productId
        Page<ProductReview> reviewPage = reviewRepository.findByProductIdAndIsApprovedTrue(productId, pageable);
        logger.debug("Found {} approved reviews for product ID: {} on page {}", reviewPage.getNumberOfElements(), productId, pageable.getPageNumber());
        // Sử dụng helper method private để map
        return reviewPage.map(this::mapToProductReviewDTO); // Dòng này giữ nguyên
    }

    @Override
    public ProductReviewDTO createReview(Integer productId, CreateReviewRequestDTO request, String username) {
        logger.info("User '{}' attempting to create review for product ID: {}", username, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found with ID: {} during review creation attempt by user '{}'.", productId, username);
                    return new ResourceNotFoundException("Sản phẩm", "id", productId);
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authenticated user '{}' not found in repository during review creation.", username);
                    return new ResourceNotFoundException("Người dùng", "username", username);
                });

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            logger.warn("User '{}' (ID: {}) already reviewed product ID: {}. Denying creation.", username, user.getId(), productId);
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi.");
        }

        // (Optional purchase check logic here)

        ProductReview newReview = new ProductReview();
        newReview.setProduct(product);
        newReview.setUser(user);
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Điểm đánh giá phải từ 1 đến 5.");
        }
        newReview.setRating(request.getRating());
        newReview.setComment(request.getComment() != null ? request.getComment().trim() : null);
        // Giả sử isApproved mặc định là true hoặc được xử lý bởi entity listener/default value

        ProductReview savedReview = reviewRepository.save(newReview);
        logger.info("Review created successfully with ID: {} by user '{}' (ID: {}) for product ID: {}",
                savedReview.getId(), username, user.getId(), productId);

        // (Optional updateProductAverageRating logic here)

        return mapToProductReviewDTO(savedReview); // Dòng này giữ nguyên
    }

    // --- Private Helper Methods ---

    /**
     * Chuyển đổi một ProductReview entity sang ProductReviewDTO.
     * **Đã sửa: Loại bỏ trường isApproved không tồn tại trong DTO.**
     * @param review ProductReview entity.
     * @return ProductReviewDTO.
     */
    private ProductReviewDTO mapToProductReviewDTO(ProductReview review) {
        if (review == null) {
            return null;
        }
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        if (review.getProduct() != null) {
            dto.setProductId(review.getProduct().getId());
        }
        if (review.getUser() != null) {
            dto.setUsername(review.getUser().getUsername());
            dto.setUserFullName(review.getUser().getFullName()); // Giữ lại trường này nếu nó tồn tại trong DTO của bạn
        } else {
            dto.setUsername("Người dùng ẩn danh");
            dto.setUserFullName("Người dùng ẩn danh"); // Giữ lại trường này nếu nó tồn tại trong DTO của bạn
            logger.warn("Review ID {} has a null user associated.", review.getId());
        }
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        // dto.setIsApproved(review.getIsApproved()); // **ĐÃ LOẠI BỎ DÒNG NÀY**

        return dto;
    }

    // (Placeholder checkUserPurchase)
    // (Placeholder updateProductAverageRating)

}