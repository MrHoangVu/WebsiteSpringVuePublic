// src/main/java/com/example/demo/dto/OrderListDTO.java
package com.example.demo.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderListDTO {
    private Integer orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private List<OrderItemSummaryDTO> items; // Danh sách tóm tắt sản phẩm
    // Thêm các trường khác nếu cần hiển thị ngay trên danh sách (VD: địa chỉ ship tóm tắt)
}