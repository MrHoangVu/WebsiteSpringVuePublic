package com.example.demo.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {
    @NotBlank(message = "New status cannot be blank")
    private String newStatus;
    // Có thể thêm các trường khác nếu cần (ghi chú nội bộ, lý do hủy,...)
}