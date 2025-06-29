
package com.example.demo.controller.admin;

import com.example.demo.dto.admin.AdminUserDetailDTO;
import com.example.demo.dto.admin.AdminUserListDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService; // Hoặc AdminUserService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // Import Map

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller
public class AdminUserController {

    private final UserService userService; // Hoặc AdminUserService

    /**
     * Lấy danh sách khách hàng (phân trang, lọc, sắp xếp).
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserListDTO>> getCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminUserListDTO> customerPage = userService.getCustomerList(keyword, tier, isActive, pageable);
        return ResponseEntity.ok(customerPage);
    }

    /**
     * Lấy chi tiết một khách hàng.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDTO> getCustomerDetail(@PathVariable Integer userId) {
        AdminUserDetailDTO customerDetail = userService.getCustomerDetail(userId);
        return ResponseEntity.ok(customerDetail);
    }

    /**
     * Cập nhật bậc (tier) của khách hàng.
     */
    @PutMapping("/{userId}/tier")
    public ResponseEntity<AdminUserListDTO> updateCustomerTier(
            @PathVariable Integer userId,
            @Valid @RequestBody Map<String, String> payload) { // Nhận tier mới qua body dạng JSON {"newTier": "GOLD"}

        String newTier = payload.get("newTier");
        if (newTier == null || newTier.isBlank()) {
            return ResponseEntity.badRequest().build(); // Hoặc trả về lỗi cụ thể
        }
        User updatedUser = userService.updateCustomerTier(userId, newTier);
        // Trả về DTO list để cập nhật nhanh trên bảng nếu cần
        return ResponseEntity.ok(userService.mapToAdminUserListDTO(updatedUser)); // Cần thêm hàm map này vào service
    }

    /**
     * Cập nhật trạng thái hoạt động (khóa/mở khóa) của khách hàng.
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<AdminUserListDTO> updateCustomerStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody Map<String, Boolean> payload) { // Nhận status mới qua body dạng JSON {"isActive": false}

        Boolean isActive = payload.get("isActive");
        if (isActive == null) {
            return ResponseEntity.badRequest().build();
        }
        User updatedUser = userService.updateCustomerStatus(userId, isActive);
        return ResponseEntity.ok(userService.mapToAdminUserListDTO(updatedUser));
    }

}