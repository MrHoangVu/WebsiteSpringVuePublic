package com.example.demo.controller.admin;

import com.example.demo.dto.shipping.ShippingMethodDTO;
import com.example.demo.service.ShippingMethodService;
import jakarta.validation.Valid; // Import validation
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shipping-methods") // Base path cho admin shipping methods
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller
public class AdminShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    /**
     * Lấy danh sách tất cả PTVC (phân trang).
     */
    @GetMapping
    public ResponseEntity<Page<ShippingMethodDTO>> getAllShippingMethods(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ShippingMethodDTO> methodPage = shippingMethodService.getAllShippingMethodsAdmin(pageable);
        return ResponseEntity.ok(methodPage);
    }

    /**
     * Lấy chi tiết một PTVC theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShippingMethodDTO> getShippingMethodById(@PathVariable Integer id) {
        ShippingMethodDTO method = shippingMethodService.getShippingMethodByIdAdmin(id);
        return ResponseEntity.ok(method);
    }

    /**
     * Tạo PTVC mới.
     */
    @PostMapping
    public ResponseEntity<ShippingMethodDTO> createShippingMethod(
            @Valid @RequestBody ShippingMethodDTO dto) { // Thêm @Valid để kích hoạt validation
        ShippingMethodDTO createdMethod = shippingMethodService.createShippingMethod(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMethod);
    }

    /**
     * Cập nhật thông tin PTVC.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShippingMethodDTO> updateShippingMethod(
            @PathVariable Integer id,
            @Valid @RequestBody ShippingMethodDTO dto) { // Thêm @Valid
        ShippingMethodDTO updatedMethod = shippingMethodService.updateShippingMethod(id, dto);
        return ResponseEntity.ok(updatedMethod);
    }

    /**
     * Xóa (soft delete) PTVC.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShippingMethod(@PathVariable Integer id) {
        shippingMethodService.deleteShippingMethod(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}