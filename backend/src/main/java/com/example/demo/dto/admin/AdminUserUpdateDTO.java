package com.example.demo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserUpdateDTO {
    // Username không cho phép sửa
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName; // Cho phép null nếu không muốn cập nhật

    @NotBlank(message = "Role is required") // Role bắt buộc khi cập nhật
    @Pattern(regexp = "ADMIN|CUSTOMER|SHIPPER", message = "Role must be ADMIN or CUSTOMER or SHIPPER")
    private String role;

    @NotNull(message = "Active status is required") // Bắt buộc phải có trạng thái active
    private Boolean isActive;
}