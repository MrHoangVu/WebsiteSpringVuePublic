package com.example.demo.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoDTO {
    private String address;
    private String phone;
    private String email;
    private String workingHours;
    // Thêm các trường khác nếu cần (mapUrl,...)
}