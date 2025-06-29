// src/main/java/com/example/demo/controller/CartController.java
package com.example.demo.controller.user;

import com.example.demo.dto.cart.AddItemRequestDTO;
import com.example.demo.dto.cart.CartDTO;
import com.example.demo.dto.cart.UpdateItemQuantityRequestDTO;
import com.example.demo.service.CartService;
import jakarta.servlet.http.HttpServletRequest; // Để lấy header
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private static final String CART_SESSION_HEADER = "X-Cart-Session-Id"; // Tên header chuẩn

    private final CartService cartService;

    // Helper lấy username hoặc null
    private String getUsername(Authentication authentication) {
        return (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()))
                ? authentication.getName()
                : null;
    }

    // Helper lấy session ID từ header
    private String getCartSessionId(HttpServletRequest request) {
        return request.getHeader(CART_SESSION_HEADER);
    }

    /**
     * Lấy giỏ hàng hiện tại.
     */
    @GetMapping
    public ResponseEntity<CartDTO> getCart(Authentication authentication, HttpServletRequest request) {
        String username = getUsername(authentication);
        String cartSessionId = getCartSessionId(request);
        logger.debug("GET /cart - User: {}, Session: {}", username, cartSessionId);
        CartDTO cart = cartService.getCartDto(username, cartSessionId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Thêm một sản phẩm vào giỏ hàng.
     */
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @Valid @RequestBody AddItemRequestDTO addItemRequest,
            Authentication authentication,
            HttpServletRequest request) {
        String username = getUsername(authentication);
        String cartSessionId = getCartSessionId(request);
        logger.info("POST /cart/items - User: {}, Session: {}, Payload: {}", username, cartSessionId, addItemRequest);
        CartDTO updatedCart = cartService.addItemToCart(username, cartSessionId, addItemRequest);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ hàng.
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable Integer productId,
            @Valid @RequestBody UpdateItemQuantityRequestDTO updateRequest,
            Authentication authentication,
            HttpServletRequest request) {
        String username = getUsername(authentication);
        String cartSessionId = getCartSessionId(request);
        logger.info("PUT /cart/items/{} - User: {}, Session: {}, Payload: {}", productId, username, cartSessionId, updateRequest);
        CartDTO updatedCart = cartService.updateItemQuantity(username, cartSessionId, productId, updateRequest.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDTO> removeItem(
            @PathVariable Integer productId,
            Authentication authentication,
            HttpServletRequest request) {
        String username = getUsername(authentication);
        String cartSessionId = getCartSessionId(request);
        logger.info("DELETE /cart/items/{} - User: {}, Session: {}", productId, username, cartSessionId);
        CartDTO updatedCart = cartService.removeItemFromCart(username, cartSessionId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * Xóa toàn bộ các sản phẩm trong giỏ hàng.
     */
    @DeleteMapping
    public ResponseEntity<CartDTO> clearCart(Authentication authentication, HttpServletRequest request) {
        String username = getUsername(authentication);
        String cartSessionId = getCartSessionId(request);
        logger.info("DELETE /cart - User: {}, Session: {}", username, cartSessionId);
        CartDTO clearedCart = cartService.clearCart(username, cartSessionId);
        return ResponseEntity.ok(clearedCart);
    }

    // === ENDPOINT MỚI CHO MERGE CART ===
    @PostMapping("/merge")
    @PreAuthorize("isAuthenticated()") // Chỉ user đã đăng nhập mới được merge
    public ResponseEntity<CartDTO> mergeCart(
            @RequestBody MergeCartRequestDTO mergeRequest, // Tạo DTO này
            Authentication authentication) {

        String username = getUsername(authentication);
        if (username == null) {
            // Dòng này gần như không bao giờ xảy ra vì có @PreAuthorize
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String guestSessionId = mergeRequest.getGuestSessionId();
        logger.info("POST /cart/merge - User: {}, Merging Session: {}", username, guestSessionId);

        if (guestSessionId == null || guestSessionId.isBlank()) {
            return ResponseEntity.badRequest().body(null); // Hoặc trả về lỗi cụ thể
        }

        CartDTO mergedCart = cartService.mergeGuestCartToUserCart(guestSessionId, username);
        return ResponseEntity.ok(mergedCart);
    }

    @Data
    private static class MergeCartRequestDTO {
        private String guestSessionId;
    }
    // ===================================
}