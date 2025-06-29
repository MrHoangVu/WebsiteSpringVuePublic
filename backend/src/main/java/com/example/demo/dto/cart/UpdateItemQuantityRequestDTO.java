// src/main/java/com/example/demo/dto/UpdateItemQuantityRequestDTO.java
package com.example.demo.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateItemQuantityRequestDTO {
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1") // Nếu muốn xóa khi quantity <= 0 thì bỏ Min(1)
    private Integer quantity;
}