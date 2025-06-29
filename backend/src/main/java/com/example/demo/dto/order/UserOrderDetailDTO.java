package com.example.demo.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserOrderDetailDTO {
    // Thông tin cơ bản
    private Integer orderId;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private String orderNote; // Ghi chú của khách hàng

    // Thông tin người nhận (lưu trên Order)
    private String shippingRecipientName;
    private String shippingRecipientPhone;
    private String shippingStreetAddress;
    private String shippingWard;
    private String shippingDistrict;
    private String shippingCity;

    // Thông tin vận chuyển
    private String shippingMethodName;
    private BigDecimal shippingCost;

    // Thông tin khuyến mãi (nếu có)
    private String promotionCode;
    private BigDecimal discountAmount;

    // Chi tiết sản phẩm (Dùng lại OrderItemSummaryDTO)
    private List<OrderItemSummaryDTO> items;

    // Tổng tiền
    private BigDecimal subtotal; // Tổng tiền hàng
    private BigDecimal totalAmount; // Tổng tiền cuối cùng (đã gồm ship, trừ KM)

    // Có thể thêm các trường khác nếu user cần, ví dụ link tracking (nếu có)
    // private String trackingUrl;
}