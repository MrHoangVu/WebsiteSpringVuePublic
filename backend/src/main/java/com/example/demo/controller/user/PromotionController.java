// src/main/java/com/example/demo/controller/PromotionController.java
package com.example.demo.controller.user;

import com.example.demo.dto.admin.ApplyPromotionRequestDTO;
import com.example.demo.dto.promotion.AppliedPromotionDTO;
import com.example.demo.service.PromotionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/promotions") // Endpoint chung cho promotion
@RequiredArgsConstructor
public class PromotionController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionController.class);
    private final PromotionService promotionService;
    private static final String CART_SESSION_HEADER = "X-Cart-Session-Id"; // <<< Định nghĩa tên header

    /**
     * Xác thực và tính toán giảm giá khi áp dụng mã.
     * Backend sẽ tự lấy tổng tiền giỏ hàng.
     * @param request DTO chứa mã code.
     * @param httpRequest Đối tượng request để lấy header.
     * @return AppliedPromotionDTO chứa kết quả.
     */
    @PostMapping("/apply")
    public ResponseEntity<AppliedPromotionDTO> applyPromotion(
            @Valid @RequestBody ApplyPromotionRequestDTO request,
            HttpServletRequest httpRequest) { // <<< Inject HttpServletRequest

        // Lấy thông tin user hoặc session
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Kiểm tra xem user đã được xác thực và không phải là anonymous
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
            logger.debug("Apply promotion request from authenticated user: {}", username);
        }

        // Lấy session ID từ header (quan trọng cho guest)
        String cartSessionId = httpRequest.getHeader(CART_SESSION_HEADER);
        if (!StringUtils.hasText(cartSessionId) && username == null) {
            logger.warn("Apply promotion request missing User authentication and Cart Session ID.");
            // Trả về lỗi nếu không xác định được giỏ hàng
            AppliedPromotionDTO errorResult = new AppliedPromotionDTO(false, "Không thể xác định giỏ hàng của bạn.", null, BigDecimal.ZERO, BigDecimal.ZERO, null);
            return ResponseEntity.badRequest().body(errorResult);
        }
        logger.debug("Apply promotion request with Session ID: {}", cartSessionId);


        // Gọi service, truyền cả username và sessionId
        // Service sẽ ưu tiên username nếu có để tìm cart
        AppliedPromotionDTO result = promotionService.validateAndCalculateDiscount(
                request.getCode(),
                username,
                cartSessionId
        );

        // Trả về kết quả validation (có thể là lỗi hoặc thành công)
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            // Trả về lỗi Bad Request nếu mã không hợp lệ hoặc không đủ điều kiện
            return ResponseEntity.badRequest().body(result);
        }
    }
}