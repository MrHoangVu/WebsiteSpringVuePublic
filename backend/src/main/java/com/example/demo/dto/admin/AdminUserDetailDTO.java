package com.example.demo.dto.admin;

import com.example.demo.dto.order.OrderListDTO;
import com.example.demo.dto.user.AddressDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserDetailDTO {
    private Integer id;
    private String username;
    private String fullName;
    // private String email;
    private LocalDateTime createdAt;
    private String tier;
    private BigDecimal totalSpent;
    private Boolean isActive;
    private String role;

    // Thông tin chi tiết hơn
    private List<AddressDTO> addresses; // Danh sách địa chỉ
    private List<OrderListDTO> recentOrders; // Danh sách đơn hàng gần đây
    // Thêm các thông tin khác nếu cần
}