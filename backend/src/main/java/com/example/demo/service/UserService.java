// src/main/java/com/example/demo/service/UserService.java
package com.example.demo.service;

import com.example.demo.dto.admin.AdminUserDetailDTO;
import com.example.demo.dto.admin.AdminUserListDTO;
import com.example.demo.entity.User; // Import User nếu cần trả về entity
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Import các DTO mới
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.dto.user.UpdateUserProfileDTO;
import com.example.demo.dto.user.ChangePasswordDTO;

public interface UserService { // Hoặc AdminUserService nếu bạn tách riêng

    // --- ADMIN METHODS ---
    Page<AdminUserListDTO> getCustomerList(String keyword, String tier, Boolean isActive, Pageable pageable);
    AdminUserDetailDTO getCustomerDetail(Integer userId);
    User updateCustomerTier(Integer userId, String newTier);
    User updateCustomerStatus(Integer userId, boolean isActive);
    void updateTotalSpentAndTier(Integer userId, java.math.BigDecimal orderAmount);

    // --- USER PROFILE METHODS ---
    /**
     * Lấy thông tin chi tiết của người dùng đang đăng nhập.
     * @param username Tên đăng nhập của người dùng hiện tại.
     * @return UserProfileDTO chứa thông tin.
     */
    UserProfileDTO getCurrentUserProfile(String username);

    /**
     * Cập nhật thông tin cơ bản của người dùng đang đăng nhập.
     * @param username Tên đăng nhập của người dùng hiện tại.
     * @param dto Dữ liệu cần cập nhật (ví dụ: fullName).
     * @return UserProfileDTO đã được cập nhật.
     */
    UserProfileDTO updateUserProfile(String username, UpdateUserProfileDTO dto);

    /**
     * Thay đổi mật khẩu cho người dùng đang đăng nhập.
     * @param username Tên đăng nhập của người dùng hiện tại.
     * @param dto Dữ liệu mật khẩu mới và cũ.
     * @throws com.example.demo.exception.BadRequestException nếu mật khẩu cũ không đúng hoặc mật khẩu mới không khớp.
     */
    void changePassword(String username, ChangePasswordDTO dto);

    // --- HELPER MAPPER (Cần thiết cho AdminUserController) ---
    AdminUserListDTO mapToAdminUserListDTO(User user); // Đảm bảo hàm này tồn tại và public
}