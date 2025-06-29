// src/http/adminPromotionService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách tất cả khuyến mãi cho admin (phân trang).
 * @param {object} params { page, size, sort }
 * @returns Promise<Page<PromotionDTO>>
 */
export const getAdminPromotions = (params) => {
  return apiClient.get("/admin/promotions", { params });
};

/**
 * Lấy chi tiết khuyến mãi theo ID cho admin.
 * @param {number} id ID của khuyến mãi.
 * @returns Promise<PromotionDTO>
 */
export const getAdminPromotionById = (id) => {
  if (!id) return Promise.reject(new Error("Promotion ID is required"));
  return apiClient.get(`/admin/promotions/${id}`);
};

/**
 * Tạo khuyến mãi mới.
 * @param {object} promotionData Dữ liệu khuyến mãi (CreateUpdatePromotionDTO).
 * @returns Promise<PromotionDTO>
 */
export const createAdminPromotion = (promotionData) => {
  // Frontend cần đảm bảo targetTiers là string "SILVER,GOLD" hoặc null/rỗng
  return apiClient.post("/admin/promotions", promotionData);
};

/**
 * Cập nhật khuyến mãi.
 * @param {number} id ID của khuyến mãi.
 * @param {object} promotionData Dữ liệu cập nhật (CreateUpdatePromotionDTO).
 * @returns Promise<PromotionDTO>
 */
export const updateAdminPromotion = (id, promotionData) => {
  if (!id) return Promise.reject(new Error("Promotion ID is required"));
  // Frontend cần đảm bảo targetTiers là string "SILVER,GOLD" hoặc null/rỗng
  return apiClient.put(`/admin/promotions/${id}`, promotionData);
};

/**
 * Xóa (soft delete - bằng cách update isActive=false) khuyến mãi.
 * @param {number} id ID của khuyến mãi.
 * @returns Promise<PromotionDTO> (Hoặc Promise<void> tùy backend)
 */
export const deactivateAdminPromotion = (id, promotionData) => {
  if (!id) return Promise.reject(new Error("Promotion ID is required"));
  const payload = { ...promotionData, isActive: false }; // Set isActive to false
  return apiClient.put(`/admin/promotions/${id}`, payload);
};

/**
 * Kích hoạt lại khuyến mãi.
 * @param {number} id ID của khuyến mãi.
 * @returns Promise<PromotionDTO>
 */
export const activateAdminPromotion = (id, promotionData) => {
  if (!id) return Promise.reject(new Error("Promotion ID is required"));
  const payload = { ...promotionData, isActive: true }; // Set isActive to true
  return apiClient.put(`/admin/promotions/${id}`, payload);
};

/**
 * Cập nhật trạng thái active/inactive của khuyến mãi.
 * @param {number} id ID của khuyến mãi.
 * @param {boolean} isActive Trạng thái mới (true hoặc false).
 * @returns Promise<PromotionDTO>
 */
export const toggleAdminPromotionStatus = (id, isActive) => {
  if (id === undefined || id === null) return Promise.reject(new Error("Promotion ID is required"));
  if (isActive === undefined || isActive === null)
    return Promise.reject(new Error("Active status is required"));
  // Gọi endpoint PATCH mới mà bạn đã tạo ở backend
  return apiClient.patch(`/admin/promotions/${id}/status`, { isActive });
};

// Lưu ý: Không có API delete cứng để tránh ảnh hưởng đơn hàng cũ.
// Việc xóa hẳn có thể làm thủ công trong DB nếu thực sự cần.
