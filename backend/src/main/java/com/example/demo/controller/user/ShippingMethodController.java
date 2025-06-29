package com.example.demo.controller.user;

import com.example.demo.dto.shipping.ShippingMethodDTO;
import com.example.demo.service.ShippingMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    /**
     * Endpoint PUBLIC để lấy danh sách các phương thức vận chuyển đang hoạt động.
     * Dùng cho cả guest và user trong trang checkout.
     */
    @GetMapping("/active")
    public ResponseEntity<List<ShippingMethodDTO>> getActiveMethods() {
        List<ShippingMethodDTO> methods = shippingMethodService.getActiveShippingMethods();
        return ResponseEntity.ok(methods);
    }

    // Có thể thêm các endpoint khác cho admin quản lý PTVC sau này
    // ví dụ: GET /, POST /, PUT /{id}, DELETE /{id} (bảo vệ bằng hasRole('ADMIN'))
}