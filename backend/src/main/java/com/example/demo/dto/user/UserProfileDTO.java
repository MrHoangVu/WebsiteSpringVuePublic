package com.example.demo.dto.user;
import lombok.Data;
import java.time.LocalDateTime;
// Import các DTO khác nếu cần lồng vào (AddressDTO, OrderListDTO)

@Data
public class UserProfileDTO {
    private Integer id;
    private String username;
    private String fullName;
    // private String email; // Nếu có email
    private LocalDateTime createdAt;
    private String tier; // Thêm bậc khách hàng
    private java.math.BigDecimal totalSpent; // Thêm tổng chi tiêu
    // Có thể thêm danh sách địa chỉ hoặc đơn hàng gần đây nếu muốn hiển thị ngay
    // private java.util.List<AddressDTO> addresses;
    // private java.util.List<OrderListDTO> recentOrders;
}