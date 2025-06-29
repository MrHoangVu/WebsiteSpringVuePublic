// src/http/adminUserService.js
import apiClient from "@/http/axios.js"; // <<< Chỉ có MỘT dòng import này ở đầu

/**
 * Lấy danh sách khách hàng cho admin (phân trang, lọc, sắp xếp).
 * @param {object} params { page, size, sort, keyword?, tier?, isActive? }
 * @returns Promise<Page<AdminUserListDTO>>
 */
export const getAdminCustomers = (params) => {
  return apiClient.get("/admin/users", { params });
};

/**
 * Lấy chi tiết khách hàng cho admin.
 * @param {number} userId ID khách hàng.
 * @returns Promise<AdminUserDetailDTO>
 */
export const getAdminCustomerDetail = (userId) => {
  if (!userId) return Promise.reject(new Error("User ID is required"));
  return apiClient.get(`/admin/users/${userId}`);
};

/**
 * Cập nhật bậc (tier) của khách hàng.
 * @param {number} userId ID khách hàng.
 * @param {string} newTier Bậc mới ('BRONZE', 'SILVER', 'GOLD', 'DIAMOND').
 * @returns Promise<AdminUserListDTO>
 */
export const updateAdminCustomerTier = (userId, newTier) => {
  if (!userId) return Promise.reject(new Error("User ID is required"));
  if (!newTier) return Promise.reject(new Error("New tier is required"));
  return apiClient.put(`/admin/users/${userId}/tier`, { newTier });
};

/**
 * Cập nhật trạng thái hoạt động (khóa/mở khóa) của khách hàng.
 * @param {number} userId ID khách hàng.
 * @param {boolean} isActive Trạng thái mới (true: active, false: inactive).
 * @returns Promise<AdminUserListDTO>
 */
export const updateAdminCustomerStatus = (userId, isActive) => {
  if (!userId) return Promise.reject(new Error("User ID is required"));
  if (isActive === undefined || isActive === null)
    return Promise.reject(new Error("Active status is required"));
  return apiClient.put(`/admin/users/${userId}/status`, { isActive });
};
