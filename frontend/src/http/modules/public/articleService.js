// src/http/articleService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách bài viết đã publish (phân trang).
 * @param {object} params { page: number, size: number, sort: string }
 * @returns Promise
 */
export const getArticles = (params) => {
  return apiClient.get("/articles", { params });
};

/**
 * Lấy chi tiết bài viết đã publish theo slug.
 * @param {string} slug Slug của bài viết.
 * @returns Promise
 */
export const getArticleBySlug = (slug) => {
  if (!slug) return Promise.reject(new Error("Article slug is required"));
  return apiClient.get(`/articles/${slug}`);
};

/**
 * Tạo bài viết mới.
 * @param {object} articleData { title: string, content: string, excerpt?: string, featuredImageUrl?: string }
 * @returns Promise
 */
export const createArticle = (articleData) => {
  return apiClient.post("/articles", articleData);
};

// Thêm các hàm cho admin sau này (update, delete, publish...)
