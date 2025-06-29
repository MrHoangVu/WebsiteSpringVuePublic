// src/http/productService.js
import apiClient from "@/http/axios.js"; // Import instance Axios đã cấu hình

export const getProducts = (params) => {
  // params có thể chứa: categoryId, keyword, page, size, sort
  return apiClient.get("/products", { params }); // Gửi params trong query string
};

export const getProductBySlug = (slug) => {
  return apiClient.get(`/products/${slug}`);
};

// Thêm các hàm gọi API khác cho product nếu cần
