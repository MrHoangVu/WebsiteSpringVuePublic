package com.example.demo.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDataPointDTO {
    private LocalDate date;     // Ngày
    private BigDecimal revenue; // Doanh thu ngày đó
}