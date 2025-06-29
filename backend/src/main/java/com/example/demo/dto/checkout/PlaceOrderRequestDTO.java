// src/main/java/com/example/demo/dto/PlaceOrderRequestDTO.java
package com.example.demo.dto.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlaceOrderRequestDTO {

    // --- Địa chỉ giao hàng ---
    private Long selectedShippingAddressId; // User logged in chọn
    @Valid // Validate nếu nhập mới
    private ShippingAddressInput shippingAddressInput; // Guest hoặc User nhập mới

    // --- Phương thức vận chuyển ---
    @NotNull(message = "Shipping method ID is required")
    private Integer shippingMethodId;

    // --- Phương thức thanh toán ---
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // Ví dụ: "COD", "BANK_TRANSFER", "VNPAY",...

    // --- Khuyến mãi ---
    // Client chỉ nên gửi code hoặc ID đã được validate trước đó ở trang giỏ hàng/checkout
    private String appliedPromotionCode; // Code đã áp dụng
    // private Integer appliedPromotionId; // Hoặc ID (nếu frontend có ID)

    // --- Ghi chú ---
    @Size(max = 500, message = "Order note cannot exceed 500 characters")
    private String orderNote;

    // --- DTO con cho nhập địa chỉ ---
    @Data
    public static class ShippingAddressInput {
        @NotBlank(message = "Recipient name is required")
        @Size(max = 150)
        private String recipientName;

        @NotBlank(message = "Recipient phone is required")
        @Size(max = 20)
        private String recipientPhone;

        // Thêm Email cho guest khi nhập địa chỉ mới
        @Email(message = "Invalid email format")
        @Size(max = 255)
        private String recipientEmail; // Sẽ được dùng nếu là guest checkout

        @NotBlank(message = "Street address is required")
        @Size(max = 500)
        private String streetAddress;

        @Size(max = 100)
        private String ward; // Phường/Xã

        @NotBlank(message = "District is required")
        @Size(max = 100)
        private String district; // Quận/Huyện

        @NotBlank(message = "City is required")
        @Size(max = 100)
        private String city; // Tỉnh/Thành phố

        // Không cần country nếu mặc định là VN
    }

    // Custom validation: Đảm bảo có cách xác định địa chỉ
    @AssertTrue(message = "Shipping address information is required (select saved or enter new)")
    public boolean isShippingAddressProvided() {
        return selectedShippingAddressId != null || shippingAddressInput != null;
    }

    // Custom validation: Không được chọn cả hai cách
    @AssertTrue(message = "Cannot select a saved address AND enter a new shipping address simultaneously")
    public boolean isOnlyOneAddressMethod() {
        return !(selectedShippingAddressId != null && shippingAddressInput != null);
    }

    // Custom validation: Email là bắt buộc cho guest nếu nhập địa chỉ mới
    // Sẽ kiểm tra logic này trong Service vì cần biết user có đăng nhập hay không
}