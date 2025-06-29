import apiClient from "@/http/axios.js";

/**
 * Lấy danh sách các phương thức vận chuyển đang hoạt động (Public API).
 * @returns Promise<List<ShippingMethodDTO>>
 */
export const getActiveShippingMethods = () => {
  return apiClient.get("/shipping-methods/active");
};
