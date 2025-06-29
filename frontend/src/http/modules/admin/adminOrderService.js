import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách tất cả đơn hàng cho admin (phân trang).
 * @param {object} params { page, size, sort }
 */
export const getAllOrders = (params) => {
  return apiClient.get("/admin/orders", { params });
};

/**
 * Lấy chi tiết đơn hàng cho admin.
 * @param {number} orderId ID đơn hàng.
 */
export const getAdminOrderDetail = (orderId) => {
  if (!orderId) return Promise.reject(new Error("Order ID is required"));
  return apiClient.get(`/admin/orders/${orderId}`);
};

/**
 * Cập nhật trạng thái đơn hàng.
 * @param {number} orderId ID đơn hàng.
 * @param {string} newStatus Trạng thái mới.
 */
export const updateOrderStatus = (orderId, newStatus) => {
  if (!orderId) return Promise.reject(new Error("Order ID is required"));
  if (!newStatus) return Promise.reject(new Error("New status is required"));
  return apiClient.put(`/admin/orders/${orderId}/status`, { newStatus });
};
