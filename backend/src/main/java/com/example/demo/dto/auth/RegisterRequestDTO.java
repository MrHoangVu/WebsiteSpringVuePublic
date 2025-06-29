package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password; // Frontend sẽ gửi password đã confirm

    // Tùy chọn: Có thể thêm các trường khác
    @Size(max = 150, message = "Full name must be less than 150 characters")
    private String fullName;

    // @NotBlank(message = "Email is required") // Bỏ NotBlank nếu email không bắt buộc
    // @Email(message = "Invalid email format")
    // @Size(max = 100, message = "Email must be less than 100 characters")
    // private String email;
}