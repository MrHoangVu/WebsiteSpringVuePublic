// src/main/java/com/example/demo/controller/UserController.java (Ví dụ)
package com.example.demo.controller.user;

import com.example.demo.dto.user.ChangePasswordDTO;
import com.example.demo.dto.user.UpdateUserProfileDTO;
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Base path cho API liên quan đến user
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Lấy thông tin hồ sơ của người dùng đang đăng nhập.
     * Yêu cầu đã xác thực.
     * @param authentication Đối tượng chứa thông tin user đang đăng nhập.
     * @return UserProfileDTO.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Đảm bảo user đã đăng nhập
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName(); // Lấy username từ Principal
        logger.info("GET /api/users/me - Fetching profile for user: {}", username);
        UserProfileDTO userProfile = userService.getCurrentUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Cập nhật thông tin hồ sơ của người dùng đang đăng nhập.
     * @param authentication Đối tượng chứa thông tin user đang đăng nhập.
     * @param dto Dữ liệu cập nhật (chỉ chứa các trường được phép thay đổi).
     * @return UserProfileDTO đã được cập nhật.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileDTO dto) {
        String username = authentication.getName();
        logger.info("PUT /api/users/me - Updating profile for user: {}", username);
        UserProfileDTO updatedProfile = userService.updateUserProfile(username, dto);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Thay đổi mật khẩu của người dùng đang đăng nhập.
     * @param authentication Đối tượng chứa thông tin user đang đăng nhập.
     * @param dto Dữ liệu mật khẩu cũ và mới.
     * @return ResponseEntity 200 OK nếu thành công.
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO dto) {
        String username = authentication.getName();
        logger.info("PUT /api/users/me/password - Attempting password change for user: {}", username);
        userService.changePassword(username, dto);
        logger.info("Password changed successfully for user: {}", username);
        return ResponseEntity.ok().build(); // Trả về 200 OK, không cần body
    }

    // Có thể thêm các endpoint khác liên quan đến user ở đây
    // Ví dụ: GET /api/users/{id} (cho admin xem profile user khác) - nhưng đã có trong AdminUserController
}