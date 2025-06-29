// src/main/java/com/example/demo/dto/CartDTO.java
package com.example.demo.dto.cart;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CartDTO {
    private Long cartId; // ID của giỏ hàng (có thể hữu ích ở frontend)
    private List<CartItemDTO> items = new ArrayList<>();
    private Integer totalItems; // Tổng số lượng các sản phẩm (tính cả quantity)
    private BigDecimal subtotal; // Tổng tiền hàng (chưa tính ship, KM)

    public CartDTO(Long cartId) {
        this.cartId = cartId;
        this.totalItems = 0;
        this.subtotal = BigDecimal.ZERO;
    }

    // Helper để tính toán khi build DTO (nếu cần)
    public void calculateTotals() {
        this.totalItems = items.stream().mapToInt(CartItemDTO::getQuantity).sum();
        this.subtotal = items.stream()
                .map(CartItemDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}