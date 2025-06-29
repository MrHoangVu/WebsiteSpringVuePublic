package com.example.demo.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderListDTO {
    private Integer orderId;
    private LocalDateTime orderDate;
    private String customerInfo; // Tên user hoặc email/tên guest
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    // Thêm các trường cần thiết khác cho danh sách admin
}