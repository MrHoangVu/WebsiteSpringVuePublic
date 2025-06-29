package com.example.demo.dto.shipping;

import jakarta.validation.constraints.*; // Thêm validation
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShippingMethodDTO {
    private Integer id;

    @NotBlank(message = "Tên phương thức vận chuyển là bắt buộc")
    @Size(max = 100, message = "Tên không quá 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả không quá 255 ký tự")
    private String description;

    @NotNull(message = "Phí vận chuyển là bắt buộc")
    @DecimalMin(value = "0.0", message = "Phí vận chuyển không được âm")
    private BigDecimal baseCost;

    @Min(value = 0, message = "Số ngày dự kiến không được âm")
    private Integer estimatedDaysMin;

    @Min(value = 0, message = "Số ngày dự kiến không được âm")
    private Integer estimatedDaysMax;

    private String estimatedDelivery; // Trường này chỉ dùng để hiển thị, không cần gửi lên

    @NotNull(message = "Trạng thái hoạt động là bắt buộc")
    private Boolean isActive = true; // Thêm trường này cho admin

    // Custom validation: max days >= min days (nếu cả 2 đều có)
    @AssertTrue(message = "Số ngày tối đa phải lớn hơn hoặc bằng số ngày tối thiểu")
    public boolean isMaxDaysValid() {
        if (estimatedDaysMin != null && estimatedDaysMax != null) {
            return estimatedDaysMax >= estimatedDaysMin;
        }
        return true; // Hợp lệ nếu một trong hai hoặc cả hai là null
    }
}