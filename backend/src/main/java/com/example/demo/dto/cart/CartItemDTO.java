// src/main/java/com/example/demo/dto/CartItemDTO.java
package com.example.demo.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer productId;
    private String productName;
    private String slug; // Thêm slug để tạo link
    private String imageUrl; // Thêm ảnh để hiển thị
    private Integer quantity;
    private BigDecimal price; // Giá đơn vị tại thời điểm lấy giỏ hàng
    private BigDecimal lineTotal; // Tổng tiền của dòng này (price * quantity)
}