package com.example.demo.dto.user;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank(message = "Mật khẩu cũ là bắt buộc")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới là bắt buộc")
    @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6 đến 100 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới là bắt buộc")
    private String confirmNewPassword;
}