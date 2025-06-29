// src/http/adminArticleService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách bài viết cho admin (phân trang, lọc).
 * @param {object} params { page, size, sort, keyword?, isPublished? }
 * @returns Promise<Page<ArticleSummaryDTO>>
 */
export const getAdminArticles = (params) => {
  return apiClient.get("/admin/articles", { params });
};

/**
 * Lấy chi tiết bài viết theo ID cho admin.
 * @param {number} id ID bài viết.
 * @returns Promise<ArticleDetailDTO>
 */
export const getAdminArticleById = (id) => {
  if (!id) return Promise.reject(new Error("Article ID is required"));
  return apiClient.get(`/admin/articles/${id}`);
};

/**
 * Tạo bài viết mới.
 * @param {object} articleData Dữ liệu bài viết (ArticleCreateUpdateDTO).
 * @returns Promise<ArticleDetailDTO>
 */
export const createAdminArticle = (articleData) => {
  return apiClient.post("/admin/articles", articleData);
};

/**
 * Cập nhật bài viết.
 * @param {number} id ID bài viết.
 * @param {object} articleData Dữ liệu cập nhật (ArticleCreateUpdateDTO).
 * @returns Promise<ArticleDetailDTO>
 */
export const updateAdminArticle = (id, articleData) => {
  if (!id) return Promise.reject(new Error("Article ID is required"));
  return apiClient.put(`/admin/articles/${id}`, articleData);
};

/**
 * Xóa bài viết.
 * @param {number} id ID bài viết.
 * @returns Promise<void>
 */
export const deleteAdminArticle = (id) => {
  if (!id) return Promise.reject(new Error("Article ID is required"));
  return apiClient.delete(`/admin/articles/${id}`);
};

/**
 * Publish bài viết.
 * @param {number} id ID bài viết.
 * @returns Promise<ArticleDetailDTO>
 */
export const publishAdminArticle = (id) => {
  if (!id) return Promise.reject(new Error("Article ID is required"));
  return apiClient.patch(`/admin/articles/${id}/publish`);
};

/**
 * Unpublish bài viết.
 * @param {number} id ID bài viết.
 * @returns Promise<ArticleDetailDTO>
 */
export const unpublishAdminArticle = (id) => {
  if (!id) return Promise.reject(new Error("Article ID is required"));
  return apiClient.patch(`/admin/articles/${id}/unpublish`);
};
