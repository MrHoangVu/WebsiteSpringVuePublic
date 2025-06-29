// src/main/java/com/example/demo/service/impl/StatsServiceImpl.java
package com.example.demo.service.impl; // Đặt trong package con "impl"

import com.example.demo.dto.stats.RevenueDataPointDTO;
import com.example.demo.dto.stats.StatsDTO;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.StatsService; // Import interface
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service // Annotation @Service ở đây
@RequiredArgsConstructor // Lombok annotation
@Transactional(readOnly = true) // Transaction mặc định chỉ đọc
public class StatsServiceImpl implements StatsService { // Implement interface

    private static final Logger logger = LoggerFactory.getLogger(StatsServiceImpl.class); // Logger dùng tên class Impl

    private final OrderRepository orderRepository;
    // Inject các repository khác nếu cần thêm thống kê
    // private final ProductRepository productRepository;
    // private final UserRepository userRepository;

    @Override // Thêm @Override
    public StatsDTO getBasicStats(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching basic stats from {} to {}", startDate, endDate);

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            logger.error("Invalid date range provided for getBasicStats: start={}, end={}", startDate, endDate);
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không hợp lệ.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Sử dụng các phương thức đã định nghĩa trong OrderRepository
        long totalOrders = orderRepository.countOrdersBetweenDates(startDateTime, endDateTime);
        BigDecimal totalRevenue = orderRepository.sumTotalAmountBetweenDates(startDateTime, endDateTime);

        StatsDTO stats = new StatsDTO();
        stats.setTotalOrders(totalOrders);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO); // Xử lý null

        // (Thêm logic thống kê khác nếu cần)
        // ...

        logger.info("Basic stats result for {} to {}: {}", startDate, endDate, stats);
        return stats;
    }

    @Override // Thêm @Override
    public StatsDTO getTodayStats() {
        LocalDate today = LocalDate.now();
        logger.info("Fetching stats for today ({})", today);
        // Gọi lại phương thức chính với ngày hôm nay
        return getBasicStats(today, today);
    }

    @Override // Thêm @Override
    public StatsDTO getLast7DaysStats() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // Lùi 6 ngày để có đủ 7 ngày (tính cả hôm nay)
        logger.info("Fetching stats for the last 7 days ({} to {})", sevenDaysAgo, today);
        return getBasicStats(sevenDaysAgo, today);
    }

    @Override // Thêm @Override
    public StatsDTO getCurrentMonthStats() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        // Không cần lấy ngày cuối tháng vì getBasicStats sẽ xử lý đến cuối ngày của endDate
        logger.info("Fetching stats for the current month ({} to {})", firstDayOfMonth, today);
        // Lấy từ đầu tháng đến ngày hiện tại của tháng đó
        return getBasicStats(firstDayOfMonth, today);

        // Hoặc nếu muốn lấy cả tháng (kể cả những ngày chưa tới):
        // LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        // logger.info("Fetching stats for the entire current month ({} to {})", firstDayOfMonth, lastDayOfMonth);
        // return getBasicStats(firstDayOfMonth, lastDayOfMonth);
    }

    @Override // Thêm @Override
    public List<RevenueDataPointDTO> getRevenueOverTime(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching daily revenue data points from {} to {}", startDate, endDate);

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            logger.error("Invalid date range provided for getRevenueOverTime: start={}, end={}", startDate, endDate);
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không hợp lệ.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Lấy kết quả thô từ repository (List<Object[]>, với Object[0] là Date/Timestamp, Object[1] là BigDecimal)
        List<Object[]> results = orderRepository.findDailyRevenueBetweenDates(startDateTime, endDateTime);
        logger.debug("Raw daily revenue results count: {}", results.size());

        // Chuyển đổi kết quả thô thành Map<LocalDate, BigDecimal> để dễ truy cập
        Map<LocalDate, BigDecimal> revenueMap = results.stream()
                .filter(record -> record != null && record.length >= 2 && record[0] != null && record[1] instanceof BigDecimal) // Thêm kiểm tra null và kiểu dữ liệu
                .collect(Collectors.toMap(
                        record -> {
                            // Xử lý kiểu dữ liệu Date/Timestamp trả về từ DB
                            Object dateObject = record[0];
                            if (dateObject instanceof java.sql.Date sqlDate) {
                                return sqlDate.toLocalDate();
                            } else if (dateObject instanceof java.sql.Timestamp sqlTimestamp) {
                                return sqlTimestamp.toLocalDateTime().toLocalDate();
                            } else if (dateObject instanceof LocalDate localDate){ // Nếu repo trả về LocalDate trực tiếp
                                return localDate;
                            }
                            // Nếu kiểu không xác định, log lỗi và bỏ qua record này
                            logger.error("Unexpected date type returned from repository for revenue data: {}", dateObject.getClass().getName());
                            // Trả về một giá trị không thể là key (ví dụ: null) để Collectors.toMap bỏ qua nó, hoặc xử lý khác
                            // Tạm thời ném lỗi để biết vấn đề
                            throw new IllegalStateException("Unexpected date type in daily revenue query result: " + dateObject.getClass().getName());
                        },
                        record -> (BigDecimal) record[1], // Value là Revenue
                        (revenue1, revenue2) -> revenue1.add(revenue2) // Xử lý nếu có 2 record cùng ngày (không nên xảy ra với group by date)
                ));
        logger.debug("Revenue map created with {} entries.", revenueMap.size());

        List<RevenueDataPointDTO> revenueDataPoints = new ArrayList<>();
        long numberOfDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        logger.debug("Generating revenue data points for {} days.", numberOfDays);

        // Lặp qua tất cả các ngày trong khoảng thời gian yêu cầu
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Lấy doanh thu từ map, nếu không có thì mặc định là 0
            BigDecimal dailyRevenue = revenueMap.getOrDefault(date, BigDecimal.ZERO);
            revenueDataPoints.add(new RevenueDataPointDTO(date, dailyRevenue));
        }

        // Kiểm tra lại số lượng điểm dữ liệu khớp với số ngày
        if (revenueDataPoints.size() != numberOfDays) {
            logger.warn("Mismatch in expected number of data points ({}) and generated points ({}). Check date iteration logic.", numberOfDays, revenueDataPoints.size());
        }

        logger.info("Generated {} revenue data points for the period {} to {}", revenueDataPoints.size(), startDate, endDate);
        return revenueDataPoints;
    }
}