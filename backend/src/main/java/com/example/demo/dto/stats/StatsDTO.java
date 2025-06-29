package com.example.demo.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    private long totalOrders;
    private BigDecimal totalRevenue;
    // private List<TopSellingProductDTO> topSellingProducts; // Có thể thêm sau
    // Thêm các chỉ số khác nếu cần
}