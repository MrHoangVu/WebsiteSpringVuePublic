package com.example.demo.dto.checkout;

import com.example.demo.dto.shipping.ShippingMethodDTO;
import com.example.demo.dto.user.AddressDTO;
import lombok.Data;
import java.util.List;

@Data
public class CheckoutInfoDTO {
    private List<AddressDTO> savedAddresses; // Địa chỉ đã lưu của user
    private AddressDTO defaultShippingAddress; // Địa chỉ giao hàng mặc định
    private List<ShippingMethodDTO> availableShippingMethods; // Các PTVC khả dụng
    // Có thể thêm thông tin khác nếu cần
}