package com.example.demo.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppliedPromotionDTO {
    private boolean success; // Áp dụng thành công hay không
    private String message; // Thông báo (thành công hoặc lỗi)
    private String appliedCode; // Mã đã áp dụng (nếu thành công)
    private BigDecimal discountAmount; // Số tiền giảm giá (nếu thành công)
    private BigDecimal finalAmount; // Số tiền cuối cùng (nếu thành công) - tùy chọn
    private Integer promotionId; // ID của promotion đã áp dụng (nếu thành công) - để lưu vào Order
}