package com.example.demo.controller.user;

import com.example.demo.dto.checkout.CheckoutInfoDTO;
import com.example.demo.dto.order.OrderSummaryDTO;
import com.example.demo.dto.checkout.PlaceOrderRequestDTO;
import com.example.demo.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    // API Endpoint: GET /api/checkout/info
    // Lấy thông tin cần thiết cho trang checkout (địa chỉ, PTVC)
    // Yêu cầu đăng nhập
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutInfoDTO> getCheckoutInfo(Authentication authentication) {
        String username = authentication.getName();
        CheckoutInfoDTO checkoutInfo = checkoutService.getCheckoutInfo(username);
        return ResponseEntity.ok(checkoutInfo);
    }


    // API Endpoint: POST /api/checkout/place-order
    // API chính để đặt hàng
    @PostMapping("/place-order")
    // Cho phép cả user đã đăng nhập và guest (nếu không có PreAuthorize)
    // Nếu chỉ cho user đăng nhập: @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderSummaryDTO> placeOrder(
            @Valid @RequestBody PlaceOrderRequestDTO request,
            Authentication authentication) { // authentication có thể null nếu guest

        String username = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
        OrderSummaryDTO orderSummary = checkoutService.placeOrder(request, username);

        // Trả về 201 Created và thông tin tóm tắt đơn hàng
        return ResponseEntity.status(HttpStatus.CREATED).body(orderSummary);
    }

    // Có thể thêm API tính phí ship sau:
    // @PostMapping("/calculate-shipping") ...
}