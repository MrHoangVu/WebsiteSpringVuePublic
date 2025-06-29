// src/main/java/com/example/demo/dto/CreateUpdatePromotionDTO.java
package com.example.demo.dto.promotion;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat; // Import annotation định dạng ngày giờ
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) cho việc tạo mới hoặc cập nhật thông tin khuyến mãi.
 * Chứa các validation rules để đảm bảo dữ liệu gửi lên hợp lệ.
 */
@Data // Lombok annotation để tự động tạo getters, setters, toString, equals, hashCode
public class CreateUpdatePromotionDTO {

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 50, message = "Mã không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã chỉ được chứa chữ cái VIẾT HOA và số") // Đã ép uppercase ở frontend
    private String code;

    @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
    private String name; // Tùy chọn

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description; // Tùy chọn

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Pattern(regexp = "PERCENTAGE|FIXED_AMOUNT", message = "Loại giảm giá không hợp lệ. Sử dụng PERCENTAGE hoặc FIXED_AMOUNT")
    private String discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm giá phải là số dương") // Giảm giá phải > 0
    private BigDecimal discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là ngày hiện tại hoặc tương lai")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // ** Quan trọng: Giúp Spring hiểu định dạng ISO 8601 từ JSON/form data
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // ** Quan trọng: Giúp Spring hiểu định dạng ISO 8601
    private LocalDateTime endDate;

    // `@Future` cho endDate thường không cần thiết vì đã có validation `isEndDateAfterStartDate`
    // và việc cho phép ngày kết thúc là hôm nay thường hợp lý hơn.

    @Min(value = 1, message = "Số lượt sử dụng tối đa phải ít nhất là 1")
    private Integer maxUsage; // Tùy chọn, có thể null nếu không giới hạn

    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderValue; // Tùy chọn, có thể null nếu không yêu cầu

    // Mặc định là true khi tạo mới, client sẽ gửi giá trị hiện tại khi cập nhật
    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive = true;

    // Lưu dưới dạng chuỗi các bậc phân cách bằng dấu phẩy (VIẾT HOA), ví dụ "SILVER,GOLD".
    // Null hoặc rỗng nếu áp dụng cho tất cả các bậc.
    @Pattern(regexp = "^([A-Z]+(,[A-Z]+)*)?$", message = "Định dạng bậc không hợp lệ (VD: SILVER,GOLD hoặc để trống)")
    private String targetTiers; // Tùy chọn, có thể null

    /**
     * Validation tùy chỉnh: đảm bảo ngày kết thúc (endDate) phải sau hoặc bằng ngày bắt đầu (startDate).
     * Được kích hoạt bởi @Valid trên RequestBody trong Controller.
     *
     * @return true nếu endDate hợp lệ (sau hoặc bằng startDate) hoặc một trong hai là null, false nếu endDate trước startDate.
     */
    @AssertTrue(message = "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        // Nếu một trong hai ngày là null, validation này coi như pass (sẽ bị bắt bởi @NotNull nếu cần)
        if (startDate == null || endDate == null) {
            return true;
        }
        // Chỉ so sánh nếu cả hai ngày đều có giá trị
        // !endDate.isBefore(startDate) tương đương endDate >= startDate
        return !endDate.isBefore(startDate);
    }

    /**
     * Validation tùy chỉnh: đảm bảo giá trị giảm giá dạng phần trăm (%) hợp lệ (0 < value <= 100).
     * Được kích hoạt bởi @Valid trên RequestBody trong Controller.
     *
     * @return true nếu giá trị phần trăm hợp lệ hoặc không phải loại PERCENTAGE, false nếu không hợp lệ.
     */
    @AssertTrue(message = "Giá trị giảm giá phần trăm phải lớn hơn 0 và nhỏ hơn hoặc bằng 100")
    public boolean isValidPercentage() {
        // Chỉ áp dụng validation này nếu loại giảm giá là PERCENTAGE
        if ("PERCENTAGE".equals(discountType)) {
            // discountValue phải khác null, lớn hơn 0 và nhỏ hơn hoặc bằng 100
            return discountValue != null &&
                    discountValue.compareTo(BigDecimal.ZERO) > 0 &&
                    discountValue.compareTo(new BigDecimal("100")) <= 0;
        }
        // Nếu không phải loại PERCENTAGE, bỏ qua validation này (luôn trả về true)
        return true;
    }

    // Lưu ý: Các trường được quản lý tự động bởi hệ thống (ví dụ: id, currentUsage, createdAt, updatedAt)
    // không nên được bao gồm trong DTO này vì chúng không được gửi từ client khi tạo/cập nhật.
}