// src/http/reviewService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy các đánh giá đã duyệt của sản phẩm (phân trang).
 * @param {number} productId ID sản phẩm.
 * @param {object} params Tham số phân trang { page: number, size: number, sort: string }
 * @returns Promise chứa response từ API.
 */
export const getProductReviews = (productId, params) => {
  if (!productId) return Promise.reject(new Error("Product ID is required"));
  return apiClient.get(`/products/${productId}/reviews`, { params });
};

/**
 * Gửi đánh giá mới cho sản phẩm.
 * @param {number} productId ID sản phẩm.
 * @param {object} reviewData Dữ liệu đánh giá { rating: number, comment?: string }
 * @returns Promise chứa response từ API (review vừa tạo).
 */
export const createProductReview = (productId, reviewData) => {
  if (!productId) return Promise.reject(new Error("Product ID is required"));
  return apiClient.post(`/products/${productId}/reviews`, reviewData);
};

// Thêm các hàm khác nếu cần (xóa, sửa review - cho admin/user)
