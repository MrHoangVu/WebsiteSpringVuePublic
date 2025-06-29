package com.example.demo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyPromotionRequestDTO {
    @NotBlank(message = "Promotion code cannot be blank")
    private String code;
    // Có thể cần thêm orderId hoặc thông tin giỏ hàng ở đây tùy vào flow
    // private Long cartId;
    // private BigDecimal currentOrderTotal; // Tổng tiền hiện tại của đơn hàng để check minOrderValue
}