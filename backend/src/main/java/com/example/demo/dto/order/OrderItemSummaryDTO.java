// src/main/java/com/example/demo/dto/OrderItemSummaryDTO.java
package com.example.demo.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSummaryDTO {
    private Integer productId;
    private String productName;
    private String productImageUrl; // Thêm ảnh để hiển thị
    private Integer quantity;
    private BigDecimal priceAtPurchase; // Giá tại thời điểm mua
}