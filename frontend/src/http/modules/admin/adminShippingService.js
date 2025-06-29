// src/http/adminShippingService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách tất cả PTVC cho admin (phân trang).
 * @param {object} params { page, size, sort }
 * @returns Promise<Page<ShippingMethodDTO>>
 */
export const getAdminShippingMethods = (params) => {
  return apiClient.get("/admin/shipping-methods", { params });
};

/**
 * Lấy chi tiết PTVC theo ID cho admin.
 * @param {number} id ID của PTVC.
 * @returns Promise<ShippingMethodDTO>
 */
export const getAdminShippingMethodById = (id) => {
  if (!id) return Promise.reject(new Error("Shipping Method ID is required"));
  return apiClient.get(`/admin/shipping-methods/${id}`);
};

/**
 * Tạo PTVC mới.
 * @param {object} methodData Dữ liệu PTVC (ShippingMethodDTO).
 * @returns Promise<ShippingMethodDTO>
 */
export const createAdminShippingMethod = (methodData) => {
  return apiClient.post("/admin/shipping-methods", methodData);
};

/**
 * Cập nhật PTVC.
 * @param {number} id ID của PTVC.
 * @param {object} methodData Dữ liệu cập nhật (ShippingMethodDTO).
 * @returns Promise<ShippingMethodDTO>
 */
export const updateAdminShippingMethod = (id, methodData) => {
  if (!id) return Promise.reject(new Error("Shipping Method ID is required"));
  return apiClient.put(`/admin/shipping-methods/${id}`, methodData);
};

/**
 * Xóa (soft delete) PTVC.
 * @param {number} id ID của PTVC.
 * @returns Promise<void>
 */
export const deleteAdminShippingMethod = (id) => {
  if (!id) return Promise.reject(new Error("Shipping Method ID is required"));
  return apiClient.delete(`/admin/shipping-methods/${id}`);
};
