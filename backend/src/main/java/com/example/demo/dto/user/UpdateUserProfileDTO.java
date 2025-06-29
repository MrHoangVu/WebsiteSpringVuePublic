package com.example.demo.dto.user;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileDTO {
    @Size(max = 150, message = "Họ tên không quá 150 ký tự")
    private String fullName;
    // Thêm các trường khác cho phép cập nhật (ví dụ: email)
    // private String email;
}