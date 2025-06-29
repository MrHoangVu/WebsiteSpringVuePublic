// src/http/statsService.js
import apiClient from "@/http/axios.js"; // Import apiClient đã cấu hình (tự động gửi token)

// Lấy thống kê hôm nay
export const getTodayStats = () => {
  return apiClient.get("/admin/stats/today"); // API Backend đã tạo
};

// Lấy thống kê 7 ngày qua
export const getLast7DaysStats = () => {
  return apiClient.get("/admin/stats/last7days"); // API Backend đã tạo
};

// Lấy thống kê tháng hiện tại
export const getCurrentMonthStats = () => {
  return apiClient.get("/admin/stats/current-month"); // API Backend đã tạo
};

// Lấy thống kê theo khoảng ngày tùy chọn
// startDate và endDate phải là string dạng 'YYYY-MM-DD'
export const getBasicStats = (startDate, endDate) => {
  return apiClient.get("/admin/stats/basic", {
    params: {
      startDate, // ví dụ: '2024-01-01'
      endDate, // ví dụ: '2024-03-31'
    },
  });
};
// --- HÀM MỚI ---
// Lấy dữ liệu doanh thu theo ngày cho biểu đồ
export const getRevenueOverTime = (startDate, endDate) => {
  return apiClient.get("/admin/stats/revenue-over-time", {
    params: {
      startDate, // 'YYYY-MM-DD'
      endDate, // 'YYYY-MM-DD'
    },
  });
};
