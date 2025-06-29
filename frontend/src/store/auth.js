// src/store/auth.js
import { defineStore } from 'pinia';
import apiClient from '@/http/axios'; // Import apiClient để cấu hình header sau này
import { useCartStore } from './cart'; // <<< Import cart store ĐÃ THÊM
import { clearCartSessionId } from '@/utils/cartSession'; // <<< Import helper ĐÃ THÊM

const getStoredUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};
const getStoredToken = () => localStorage.getItem('token');

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: getStoredUser(),         // Thông tin user { username, role }
    token: getStoredToken(),       // JWT token
    returnUrl: null,               // URL để quay lại sau khi login thành công
    loginError: null,              // Lưu lỗi đăng nhập
    loading: false                 // Trạng thái loading khi gọi API login
  }),

  // Getters: Computed properties dựa trên state (GIỮ NGUYÊN TỪ FILE GỐC)
  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user, // Kiểm tra đã đăng nhập chưa
    isAdmin: (state) => state.user?.role === 'ROLE_ADMIN', // Kiểm tra có phải Admin không
    authHeader: (state) => state.token ? { Authorization: `Bearer ${state.token}` } : {} // Tạo header Auth
  },

  // Actions: Các hàm để thay đổi state (thường là bất đồng bộ)
  actions: {
    // Action đăng nhập (Logic thành công giữ nguyên, logic lỗi cập nhật)
    async login(username, password) {
      this.loading = true;
      this.loginError = null;
      try {
        // Gọi API login backend
        const response = await apiClient.post('/auth/login', { username, password });

        // Lấy dữ liệu từ response
        const { accessToken, username: loggedInUsername, role } = response.data;

        // Lưu token và user vào state
        this.token = accessToken;
        this.user = { username: loggedInUsername, role: role };

        // Lưu vào localStorage để giữ trạng thái đăng nhập sau khi tải lại trang
        localStorage.setItem('user', JSON.stringify(this.user));
        localStorage.setItem('token', this.token);

        // Cấu hình header mặc định cho các request tiếp theo của apiClient
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${this.token}`;

        this.loading = false;
        return true; // Đăng nhập thành công

      } catch (error) {
        // <<< LOGIC XỬ LÝ LỖI ĐƯỢC CẬP NHẬT TỪ FILE THỨ HAI >>>
        console.error("Login failed:", error);
        let errorMessage = 'Đã có lỗi xảy ra. Vui lòng thử lại.';
        if (error.response && error.response.status === 401) {
          errorMessage = 'Tên đăng nhập hoặc mật khẩu không đúng.';
        } else if (error.response?.data && typeof error.response.data === 'string' && error.response.data.length < 100) { // Check length too
          errorMessage = error.response.data;
        } else if (error.response?.data?.error) {
          errorMessage = error.response.data.error;
        } else if (error.response?.data?.message) {
          errorMessage = error.response.data.message;
        }
        this.loginError = errorMessage;
        this.loading = false;
        return false; // Đăng nhập thất bại
      }
    },

    // Action đăng xuất (Được cập nhật để gọi cartStore và clearCartSessionId)
    logout() {
      console.log("AuthStore: Logging out user..."); // Log logout ĐÃ THÊM

      // === Gọi cleanup của Cart Store === ĐÃ THÊM
      const cartStore = useCartStore(); // Lấy cart store
      cartStore.handleLogoutCleanup(); // Gọi action cleanup
      // ==================================

      // Xóa thông tin user và token khỏi state (GIỮ NGUYÊN TỪ FILE GỐC)
      this.user = null;
      this.token = null;
      this.returnUrl = null;
      this.loginError = null;

      // Xóa khỏi localStorage (GIỮ NGUYÊN TỪ FILE GỐC)
      localStorage.removeItem('user');
      localStorage.removeItem('token');

      // Xóa header Authorization mặc định của apiClient (GIỮ NGUYÊN TỪ FILE GỐC)
      delete apiClient.defaults.headers.common['Authorization'];

      // Xóa luôn session ID của guest nếu có khi logout (ĐÃ THÊM)
      clearCartSessionId();

      console.log("AuthStore: User logged out and data cleared."); // Log ĐÃ THÊM
      // Điều hướng sẽ thực hiện trong component gọi logout
    },

    // Action để set returnUrl (GIỮ NGUYÊN TỪ FILE GỐC)
    setReturnUrl(url) {
      this.returnUrl = url;
    }
  }
});
