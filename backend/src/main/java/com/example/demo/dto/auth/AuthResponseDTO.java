package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken; // JWT Token
    private String tokenType = "Bearer";
    private String username;
    private String role;
    // Có thể thêm thời gian hết hạn token nếu cần
}