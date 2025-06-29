package com.example.demo.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminOrderDetailDTO {
    // Thông tin cơ bản
    private Integer orderId;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private String orderNote;

    // Thông tin khách hàng
    private Integer userId; // ID user nếu có
    private String username; // Username nếu có
    private String guestEmail; // Email guest nếu có

    // Thông tin người nhận (lưu trực tiếp trên Order)
    private String shippingRecipientName;
    private String shippingRecipientPhone;
    private String shippingStreetAddress;
    private String shippingWard;
    private String shippingDistrict;
    private String shippingCity;

    // Thông tin vận chuyển
    private String shippingMethodName;
    private BigDecimal shippingCost;

    // Thông tin khuyến mãi
    private String promotionCode; // Code KM nếu có
    private BigDecimal discountAmount;

    // Chi tiết sản phẩm
    private List<OrderItemSummaryDTO> items; // Dùng lại DTO cũ

    // Tổng tiền
    private BigDecimal subtotal; // Có thể tính lại hoặc lấy từ đâu đó
    private BigDecimal totalAmount; // Tổng tiền cuối cùng

    // Thêm lịch sử trạng thái nếu có
}