// src/http/categoryService.js
import apiClient from "@/http/axios.js";

export const getAllCategories = () => {
  return apiClient.get("/categories");
};

// --- HÀM MỚI ---
// Giả định backend có API này
export const getCategoryBySlug = (slug) => {
  return apiClient.get(`/categories/slug/${slug}`);
};
