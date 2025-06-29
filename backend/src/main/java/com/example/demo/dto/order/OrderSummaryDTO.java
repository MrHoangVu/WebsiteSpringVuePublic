package com.example.demo.dto.order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder // Dùng Builder pattern để dễ tạo đối tượng
public class OrderSummaryDTO {
    private Integer orderId;
    private String orderStatus;
    private LocalDateTime orderDate;
    private BigDecimal finalAmount; // Tổng tiền cuối cùng
    private String paymentMethod;
    private String successMessage; // Thông báo thành công
    // Thêm các thông tin khác nếu cần (URL thanh toán online, mã đơn hàng...)
    private String paymentUrl; // Nếu thanh toán online
}