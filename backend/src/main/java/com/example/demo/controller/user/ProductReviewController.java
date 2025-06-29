package com.example.demo.controller.user;

import com.example.demo.dto.review.CreateReviewRequestDTO;
import com.example.demo.dto.review.ProductReviewDTO;
import com.example.demo.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Để bảo vệ endpoint POST
import org.springframework.security.core.Authentication; // Để lấy username người đang đăng nhập
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/reviews") // Endpoint chung cho reviews của một sản phẩm
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    // API Endpoint: GET /api/products/{productId}/reviews
    // Lấy danh sách review đã duyệt của sản phẩm (phân trang)
    // Ai cũng có thể xem
    @GetMapping
    public ResponseEntity<Page<ProductReviewDTO>> getProductReviews(
            @PathVariable Integer productId,
            // Mặc định 10 review/trang, sắp xếp theo ngày tạo mới nhất
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ProductReviewDTO> reviewPage = reviewService.getApprovedReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(reviewPage);
    }

    // API Endpoint: POST /api/products/{productId}/reviews
    // Tạo review mới
    // Yêu cầu đã đăng nhập (có token hợp lệ)
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Đảm bảo người dùng đã đăng nhập
    public ResponseEntity<ProductReviewDTO> createProductReview(
            @PathVariable Integer productId,
            @Valid @RequestBody CreateReviewRequestDTO reviewRequest,
            Authentication authentication) { // Inject Authentication để lấy thông tin user

        if (authentication == null || !authentication.isAuthenticated()) {
            // Dòng này thực ra không cần vì @PreAuthorize đã xử lý, nhưng để rõ ràng
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName(); // Lấy username từ principal

        ProductReviewDTO createdReview = reviewService.createReview(productId, reviewRequest, username);
        // Trả về 201 Created cùng với review vừa tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    // Có thể thêm các endpoint cho Admin sau (PUT /reviews/{reviewId}/approve, DELETE /reviews/{reviewId}, ...)
}