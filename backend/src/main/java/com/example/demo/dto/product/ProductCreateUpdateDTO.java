package com.example.demo.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductCreateUpdateDTO {

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    // Description có thể không bắt buộc hoặc có min length tùy yêu cầu
    private String description; // Sử dụng Rich Text Editor ở frontend

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0; // Mặc định là 0

    // Image URL có thể tùy chọn, nên dùng validation URL nếu bắt buộc
    @Size(max = 512, message = "Image URL cannot exceed 512 characters")
    // @URL(message = "Invalid image URL format") // Uncomment nếu bắt buộc URL hợp lệ
    private String imageUrl;

    @Size(max = 100, message = "Dimensions cannot exceed 100 characters")
    private String dimensions;

    @Size(max = 100, message = "Material cannot exceed 100 characters")
    private String material;

    @NotNull(message = "Category ID is required")
    private Integer categoryId; // ID của Category

    @NotNull(message = "Active status is required")
    private Boolean isActive = true; // Mặc định là active khi tạo

    // slug sẽ được tạo tự động ở backend
}