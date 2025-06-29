package com.example.demo.dto.user;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    // Không cần userId ở đây
    private String recipientName;
    private String recipientPhone;
    private String streetAddress;
    private String ward;
    private String district;
    private String city;
    private String country;
    private Boolean isDefaultShipping;
    private Boolean isDefaultBilling;
}