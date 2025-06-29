import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách tất cả sản phẩm cho admin (phân trang).
 * @param {object} params { page, size, sort }
 * @returns Promise<Page<ProductDetailDTO>> // Backend trả về DTO chi tiết
 */
export const getAllProductsAdmin = (params) => {
  return apiClient.get("/admin/products", { params });
};

/**
 * Lấy chi tiết một sản phẩm theo ID (kể cả inactive) cho admin.
 * @param {number} productId ID sản phẩm.
 * @returns Promise<ProductDetailDTO>
 */
export const getProductByIdAdmin = (productId) => {
  if (!productId) return Promise.reject(new Error("Product ID is required"));
  return apiClient.get(`/admin/products/${productId}`);
};

/**
 * Tạo sản phẩm mới.
 * @param {object} productData Dữ liệu sản phẩm (ProductCreateUpdateDTO)
 * @returns Promise<ProductDetailDTO> Sản phẩm vừa tạo
 */
export const createProduct = (productData) => {
  return apiClient.post("/admin/products", productData);
};

/**
 * Cập nhật thông tin sản phẩm.
 * @param {number} productId ID sản phẩm cần cập nhật.
 * @param {object} productData Dữ liệu cập nhật (ProductCreateUpdateDTO)
 * @returns Promise<ProductDetailDTO> Sản phẩm đã cập nhật
 */
export const updateProduct = (productId, productData) => {
  if (!productId) return Promise.reject(new Error("Product ID is required for update"));
  return apiClient.put(`/admin/products/${productId}`, productData);
};
/**
 * Cập nhật trạng thái active/inactive của sản phẩm.
 * @param {number} productId ID sản phẩm.
 * @param {boolean} isActive Trạng thái mới.
 * @returns Promise<ProductDetailDTO>
 */
export const toggleAdminProductStatus = (productId, isActive) => {
  if (productId === undefined || productId === null)
    return Promise.reject(new Error("Product ID is required"));
  if (isActive === undefined || isActive === null)
    return Promise.reject(new Error("Active status is required"));
  return apiClient.patch(`/admin/products/${productId}/status`, { isActive }); // Gọi API PATCH mới
};

// Hàm deleteProduct cũ có thể sửa để gọi toggleAdminProductStatus(productId, false) hoặc bỏ đi
export const deleteProduct = (productId) => {
  if (!productId) return Promise.reject(new Error("Product ID is required for deletion"));
  // Thay vì gọi DELETE, gọi API toggle để set thành inactive
  console.warn(
    "Using deleteProduct service function to deactivate. Consider using toggleAdminProductStatus directly."
  );
  return toggleAdminProductStatus(productId, false);
  // Hoặc giữ nguyên nếu backend DELETE vẫn làm soft delete:
  // return apiClient.delete(`/admin/products/${productId}`);
};
