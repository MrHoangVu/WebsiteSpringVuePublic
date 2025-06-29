// src/main/java/com/example/demo/service/impl/AuthServiceImpl.java
package com.example.demo.service.impl; // Đặt trong package con "impl"

import com.example.demo.dto.auth.RegisterRequestDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService; // Import interface
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Annotation @Service ở đây
@RequiredArgsConstructor // Lombok annotation
public class AuthServiceImpl implements AuthService { // Implement interface

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class); // Logger dùng tên class Impl

    // Dependencies
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject để mã hóa mật khẩu

    @Override // Thêm @Override
    @Transactional // Đảm bảo các thao tác DB nằm trong 1 transaction
    public User registerUser(RegisterRequestDTO registerRequest) {
        logger.info("Attempting to register user with username: {}", registerRequest.getUsername());

        // 1. Validate Input (Có thể thực hiện ở Controller hoặc validation layer)
        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getFullName() == null ||
                registerRequest.getUsername().isBlank() || registerRequest.getPassword().isBlank() || registerRequest.getFullName().isBlank()) {
            throw new BadRequestException("Thông tin đăng ký không được để trống (Username, Password, FullName).");
        }


        // 2. Kiểm tra Username tồn tại
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: Username '{}' already exists.", registerRequest.getUsername());
            throw new BadRequestException("Tên đăng nhập này đã được sử dụng.");
        }


        // 4. Tạo đối tượng User mới
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername().trim()); // Trim khoảng trắng thừa
        // 5. Mã hóa mật khẩu trước khi lưu - RẤT QUAN TRỌNG
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFullName(registerRequest.getFullName().trim()); // Trim khoảng trắng thừa
        // newUser.setEmail(registerRequest.getEmail() != null ? registerRequest.getEmail().trim() : null); // Nếu có email

        // 6. Đặt Role mặc định là CUSTOMER
        // TODO: Nên sử dụng Enum hoặc hằng số thay vì chuỗi "CUSTOMER" trực tiếp
        newUser.setRole("CUSTOMER");

        // 7. Các trường mặc định khác (nếu có)
        newUser.setIsActive(true); // Mặc định tài khoản mới là active
        // newUser.setTier("BRONZE"); // Mặc định hạng thành viên

        // 8. Lưu User vào CSDL
        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User '{}' registered successfully with ID: {}", savedUser.getUsername(), savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            // Bắt các lỗi khác có thể xảy ra khi lưu (ví dụ: constraint DB)
            logger.error("Error saving new user '{}' to database.", newUser.getUsername(), e);
            // Ném ra một exception chung hơn hoặc log và trả về null/ném lại tùy chính sách xử lý lỗi
            throw new RuntimeException("Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.", e);
        }
    }
}