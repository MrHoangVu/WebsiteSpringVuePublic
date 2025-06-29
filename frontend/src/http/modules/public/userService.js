// src/http/userService.js
import apiClient from "@/http/axios.js";

/**
 * Lấy thông tin chi tiết của người dùng đang đăng nhập.
 * @returns Promise<UserProfileDTO>
 */
export const getUserProfile = () => {
  // Token được gửi tự động bởi interceptor
  return apiClient.get("/users/me");
};

/**
 * Cập nhật thông tin cơ bản của người dùng đang đăng nhập.
 * @param {object} profileData Dữ liệu cập nhật (UpdateUserProfileDTO)
 * @returns Promise<UserProfileDTO>
 */
export const updateUserProfile = (profileData) => {
  return apiClient.put("/users/me", profileData);
};

/**
 * Thay đổi mật khẩu của người dùng đang đăng nhập.
 * @param {object} passwordData Dữ liệu mật khẩu (ChangePasswordDTO)
 * @returns Promise<void> (Hoặc thông báo thành công)
 */
export const changeUserPassword = (passwordData) => {
  return apiClient.put("/users/me/password", passwordData);
};

// Thêm các hàm API cho quản lý địa chỉ nếu cần
// export const getUserAddresses = () => apiClient.get('/addresses');
// export const addUserAddress = (addressData) => apiClient.post('/addresses', addressData);
// ...
