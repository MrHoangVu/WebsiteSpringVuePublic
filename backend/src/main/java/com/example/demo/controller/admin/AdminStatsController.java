package com.example.demo.controller.admin;


import com.example.demo.dto.stats.RevenueDataPointDTO;
import com.example.demo.dto.stats.StatsDTO;
import com.example.demo.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; // Để parse LocalDate từ param
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate; // Import LocalDate
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('ADMIN')") // Có thể bảo vệ toàn bộ controller ở đây thay vì trong SecurityConfig
public class AdminStatsController {

    private final StatsService statsService;

    // API Endpoint: GET /api/admin/stats/basic
    // Lấy thống kê cơ bản theo khoảng ngày tùy chọn
    // Yêu cầu quyền ADMIN (đã cấu hình trong SecurityConfig)
    @GetMapping("/basic")
    public ResponseEntity<StatsDTO> getBasicStats(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Kiểm tra ngày hợp lệ (ví dụ: startDate không sau endDate)
        if (startDate.isAfter(endDate)) {
            // Có thể trả về lỗi 400 Bad Request
            // return ResponseEntity.badRequest().body("Start date must be before or equal to end date.");
            // Hoặc đơn giản trả về kết quả rỗng/0
            return ResponseEntity.ok(new StatsDTO(0, java.math.BigDecimal.ZERO));
        }

        StatsDTO stats = statsService.getBasicStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    // API Endpoint: GET /api/admin/stats/today
    // Lấy thống kê cho ngày hôm nay
    @GetMapping("/today")
    public ResponseEntity<StatsDTO> getTodayStats() {
        StatsDTO stats = statsService.getTodayStats();
        return ResponseEntity.ok(stats);
    }

    // API Endpoint: GET /api/admin/stats/last7days
    // Lấy thống kê cho 7 ngày gần nhất
    @GetMapping("/last7days")
    public ResponseEntity<StatsDTO> getLast7DaysStats() {
        StatsDTO stats = statsService.getLast7DaysStats();
        return ResponseEntity.ok(stats);
    }

    // API Endpoint: GET /api/admin/stats/current-month
    // Lấy thống kê cho tháng hiện tại
    @GetMapping("/current-month")
    public ResponseEntity<StatsDTO> getCurrentMonthStats() {
        StatsDTO stats = statsService.getCurrentMonthStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue-over-time")
    public ResponseEntity<List<RevenueDataPointDTO>> getRevenueOverTime(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            // Trả về danh sách rỗng hoặc lỗi 400
            // return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(new ArrayList<>()); // Trả về list rỗng
        }
        List<RevenueDataPointDTO> data = statsService.getRevenueOverTime(startDate, endDate);
        return ResponseEntity.ok(data);
    }
}