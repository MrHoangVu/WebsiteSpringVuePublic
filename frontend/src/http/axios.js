import axios from 'axios';
import { useAuthStore } from '@/store/auth';
import { getCartSessionId } from '@/utils/cartSession';

// Khởi tạo instance Axios
const apiClient = axios.create({
  // baseURL: 'http://localhost:8080/api', // Nên lấy từ biến môi trường
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api', // Sử dụng biến môi trường
  headers: {
    'Content-Type': 'application/json',
  }
});

// ---- INTERCEPTOR REQUEST ----
apiClient.interceptors.request.use(
  config => {
    // Khởi tạo store auth BÊN TRONG interceptor
    // !!! Quan trọng: Phải đảm bảo Pinia đã được install vào app TRƯỚC KHI interceptor này chạy lần đầu
    // Điều này thường đúng nếu bạn import và use(pinia) trong main.js
    let authStore;
    try {
      authStore = useAuthStore(); // Lấy store
    } catch (error) {
      // Lỗi này xảy ra nếu store chưa sẵn sàng khi request đầu tiên được gửi
      // (ví dụ: request gửi từ file ngoài component setup)
      console.error("Axios Interceptor Error: Could not get auth store. Pinia might not be initialized yet.", error);
      // Có thể không gửi token/session trong trường hợp này hoặc throw lỗi tùy chiến lược
      return config; // Hoặc return Promise.reject(error);
    }

    // Ưu tiên gửi Token nếu đã đăng nhập
    if (authStore.isAuthenticated && authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`;
      // Xóa header session ID nếu có (không cần gửi cả 2)
      delete config.headers['X-Cart-Session-Id'];
      // console.debug('Axios Interceptor: Sending Auth Token');
    }
    // Nếu không đăng nhập, gửi Session ID
    else {
      const sessionId = getCartSessionId(); // Lấy hoặc tạo session ID
      if (sessionId) {
        config.headers['X-Cart-Session-Id'] = sessionId;
        // console.debug('Axios Interceptor: Sending Cart Session ID:', sessionId);
      } else {
        // Nếu không có sessionId (rất hiếm), không gửi header này
        console.warn('Axios Interceptor: Cart Session ID is missing.');
      }
      // Đảm bảo không gửi header Authorization cũ nếu user vừa logout
      delete config.headers['Authorization'];
    }
    return config;
  },
  error => {
    console.error("Axios Request Interceptor Error:", error);
    return Promise.reject(error);
  }
);

// ---- INTERCEPTOR RESPONSE (Xử lý lỗi 401) ----
apiClient.interceptors.response.use(
  response => response, // Trả về response nếu thành công
  error => {
    // Chỉ xử lý nếu có response lỗi từ server
    if (error.response) {
      const originalRequest = error.config; // Lấy request gốc
      const status = error.response.status;

      // Xử lý lỗi 401 Unauthorized (Token hết hạn hoặc không hợp lệ)
      if (status === 401 && !originalRequest._retry) { // Thêm cờ _retry để tránh lặp vô hạn
        console.warn("Unauthorized (401) response detected.");
        let authStore;
        try {
          authStore = useAuthStore();
        } catch (storeError) {
          console.error("Axios Response Interceptor Error: Could not get auth store for 401 handling.", storeError);
          return Promise.reject(error); // Không thể xử lý, reject lỗi gốc
        }

        // Chỉ thực hiện logout nếu user đang được coi là đã đăng nhập
        if (authStore.isAuthenticated) {
          console.log("Axios Interceptor: Logging out user due to 401.");
          authStore.logout(); // Gọi action logout (sẽ xóa token, user, cart client)

          // Redirect về trang login
          // Tránh redirect cứng, nên dùng router nếu có thể truy cập
          // Hoặc để component/guard xử lý redirect dựa trên state của authStore
          // window.location.href = '/login'; // Không nên
          // Gợi ý: Dùng một event bus hoặc state global để báo hiệu cần redirect
          if (router) { // Giả sử router được export và import vào đây
            router.push({ name: 'login', query: { sessionExpired: 'true' } }).catch(()=>{});
          } else {
            console.warn("Axios Interceptor: Router instance not available for 401 redirect.");
            // Có thể đặt 1 flag trong store để App.vue theo dõi và redirect
          }
        } else {
          console.log("Axios Interceptor: 401 received for guest or already logged out user. Ignoring.");
          // Không cần làm gì nếu guest bị 401 (có thể do session ID backend bị lỗi?)
        }
      }
      // Có thể thêm xử lý cho các mã lỗi khác ở đây (403, 500, etc.)
      // else if (status === 403) { ... }
    } else if (error.request) {
      // Lỗi không nhận được response (network error)
      console.error("Axios Network Error:", error.message);
    } else {
      // Lỗi khác
      console.error("Axios Error:", error.message);
    }

    // Luôn reject lỗi để promise chain bên ngoài có thể catch được
    return Promise.reject(error);
  }
);

export default apiClient;
