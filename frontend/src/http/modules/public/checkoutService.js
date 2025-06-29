// src/http/checkoutService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy thông tin cần thiết cho trang checkout của user đã đăng nhập.
 * (Địa chỉ đã lưu, PTVC khả dụng,...)
 * Yêu cầu user đã đăng nhập (có token).
 * @returns Promise chứa CheckoutInfoDTO
 */
export const getCheckoutInfo = () => {
  // Backend sẽ tự lấy username từ token đã được gửi bởi interceptor
  return apiClient.get("/checkout/info");
};

/**
 * Gửi yêu cầu đặt hàng.
 * @param {object} orderData Dữ liệu đặt hàng (PlaceOrderRequestDTO)
 * @returns Promise chứa OrderSummaryDTO
 */
export const placeOrder = (orderData) => {
  // Backend sẽ tự lấy username (nếu có) từ token
  // và session ID (nếu là guest) từ header X-Cart-Session-Id
  return apiClient.post("/checkout/place-order", orderData);
};

// Có thể thêm các hàm khác sau này, ví dụ:
// export const calculateShippingFee = (addressData, shippingMethodId) => { ... };
