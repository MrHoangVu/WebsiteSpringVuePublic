import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách đơn hàng của người dùng (phân trang).
 * @param {object} params { page: number, size: number, sort: string }
 * @returns Promise<Page<OrderListDTO>>
 */
export const getUserOrders = (params) => {
  // Token sẽ được tự động thêm bởi interceptor
  return apiClient.get("/orders", { params });
};

/**
 * Lấy tóm tắt chi tiết của một đơn hàng. (Có thể không cần nữa nếu dùng hàm dưới)
 * @param {number} orderId ID của đơn hàng.
 * @returns Promise<OrderSummaryDTO>
 */
export const getOrderSummaryById = (orderId) => {
  if (!orderId) return Promise.reject(new Error("Order ID is required"));
  return apiClient.get(`/orders/${orderId}/summary`);
};

// === HÀM MỚI ĐƯỢC THÊM VÀO FILE ĐÃ CÓ ===
/**
 * Lấy chi tiết đầy đủ một đơn hàng cho người dùng.
 * @param {number} orderId ID của đơn hàng.
 * @returns Promise<UserOrderDetailDTO>
 */
export const getUserOrderDetail = (orderId) => {
  if (!orderId) return Promise.reject(new Error("Order ID is required"));
  // API backend là GET /api/orders/{orderId}
  return apiClient.get(`/orders/${orderId}`); // <<< Gọi API chi tiết
};
