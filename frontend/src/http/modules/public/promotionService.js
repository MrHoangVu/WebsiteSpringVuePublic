// src/http/promotionService.js
import apiClient from "@/http/axios.js";

/**
 * Gửi mã khuyến mãi để xác thực và tính toán giảm giá.
 * @param {string} code Mã khuyến mãi cần áp dụng.
 * @returns Promise chứa response từ API (AppliedPromotionDTO).
 */
export const applyPromotionCode = (code) => {
  if (!code) {
    return Promise.reject(new Error("Promotion code cannot be empty"));
  }
  // Lưu ý quan trọng về currentOrderTotal:
  // Như đã nói ở backend, việc gửi total từ client là không an toàn.
  // API backend `/api/promotions/apply` CẦN được sửa đổi để tự lấy
  // tổng tiền từ giỏ hàng/session/DB dựa trên user hoặc cartId.
  // Tạm thời, chúng ta chỉ gửi code theo đúng DTO `ApplyPromotionRequestDTO`.
  return apiClient.post("/promotions/apply", { code });
};

// Có thể thêm hàm removePromotionCode sau này nếu cần
