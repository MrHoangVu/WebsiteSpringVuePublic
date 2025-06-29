// src/http/cartService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy thông tin giỏ hàng hiện tại.
 * Backend sẽ tự xác định user/guest dựa trên token/session header.
 */
export const getCart = () => {
  return apiClient.get("/cart");
};

/**
 * Thêm sản phẩm vào giỏ hàng.
 * @param {object} itemData { productId: number, quantity: number }
 */
export const addItemToCart = (itemData) => {
  return apiClient.post("/cart/items", itemData);
};

/**
 * Cập nhật số lượng sản phẩm trong giỏ hàng.
 * @param {number} productId ID sản phẩm.
 * @param {number} quantity Số lượng mới.
 */
export const updateCartItemQuantity = (productId, quantity) => {
  if (!productId) return Promise.reject(new Error("Product ID is required for update"));
  if (quantity === undefined || quantity === null)
    return Promise.reject(new Error("Quantity is required for update"));
  return apiClient.put(`/cart/items/${productId}`, { quantity });
};

/**
 * Xóa sản phẩm khỏi giỏ hàng.
 * @param {number} productId ID sản phẩm.
 */
export const removeCartItem = (productId) => {
  if (!productId) return Promise.reject(new Error("Product ID is required for removal"));
  return apiClient.delete(`/cart/items/${productId}`);
};

/**
 * Xóa toàn bộ giỏ hàng trên server.
 */
export const clearServerCart = () => {
  return apiClient.delete("/cart");
};

/**
 * API để backend thực hiện việc merge giỏ hàng.
 * Cần tạo endpoint này ở backend (ví dụ: POST /api/cart.js/merge)
 * Endpoint này sẽ được gọi sau khi user đăng nhập thành công.
 *
 * @param {string} guestSessionId Session ID của guest cart.js cần merge.
 * @returns Promise chứa CartDTO đã được merge của user.
 */
export const mergeGuestCart = (guestSessionId) => {
  if (!guestSessionId) return Promise.reject(new Error("Guest session ID is required for merging"));
  // Giả sử backend có endpoint POST /api/cart.js/merge để xử lý việc này
  // Backend sẽ tự lấy username từ token và sessionId từ request body/param
  // **LƯU Ý:** Backend cần implement endpoint này và logic CartService.mergeGuestCartToUserCart
  return apiClient.post("/cart/merge", { guestSessionId }); // Gửi sessionId trong body
};
