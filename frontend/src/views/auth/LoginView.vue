<template>
  <div class="login-view d-flex align-items-center justify-content-center min-vh-100 bg-light">
    <div class="card shadow-sm" style="width: 100%; max-width: 400px;">
      <div class="card-body p-4">
        <h2 class="card-title text-center mb-4">Đăng Nhập</h2>
        <form @submit.prevent="handleLogin">
          <!-- Error Message -->
          <div v-if="authStore.loginError" class="alert alert-danger" role="alert">
            {{ authStore.loginError }}
          </div>
          <!-- Username Input -->
          <div class="mb-3">
            <label for="username" class="form-label">Tên đăng nhập</label>
            <input type="text" class="form-control" id="username" v-model="username" required
                   placeholder="Nhập tên đăng nhập" :disabled="authStore.loading"/>
          </div>
          <!-- Password Input -->
          <div class="mb-3">
            <label for="password" class="form-label">Mật khẩu</label>
            <input type="password" class="form-control" id="password" v-model="password" required
                   placeholder="Nhập mật khẩu" :disabled="authStore.loading"/>
          </div>
          <!-- Submit Button -->
          <div class="d-grid">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="authStore.loading">
              <span v-if="authStore.loading" class="spinner-border spinner-border-sm me-2"
                    role="status" aria-hidden="true"></span>
              {{ authStore.loading ? 'Đang xử lý...' : 'Đăng nhập' }}
            </button>
          </div>
        </form>
        <!-- Links -->
        <div class="text-center mt-3">
          Chưa có tài khoản?
          <router-link :to="{name: 'register'}">Đăng ký ngay</router-link>
        </div>
        <div class="text-center mt-2">
          <router-link :to="{name: 'home'}">Quay lại trang chủ</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter, RouterLink} from 'vue-router'; // Đảm bảo RouterLink được import
import {useAuthStore} from '@/store/auth.js';
import {useCartStore} from '@/store/cart.js'; // <<< Import Cart Store (ĐÃ THÊM)

const username = ref('');
const password = ref('');
const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore(); // <<< Sử dụng Cart Store (ĐÃ THÊM)

const handleLogin = async () => {
  console.log('DEBUG: LoginView - Attempting login...'); // Log bắt đầu (ĐÃ ĐỔI TÊN LOG)

  const success = await authStore.login(username.value, password.value);

  console.log(`DEBUG: LoginView - Login success: ${success}`); // Log kết quả (ĐÃ ĐỔI TÊN LOG)
  // Log trạng thái store đã có trong file gốc, giữ nguyên nếu cần

  if (success) {
    console.log('DEBUG: LoginView - Login successful, attempting cart merge...'); // Log (ĐÃ THÊM)
    try {
      // === Gọi Merge Cart SAU KHI login thành công === (ĐÃ THÊM)
      await cartStore.handleLoginMerge();
      console.log('DEBUG: LoginView - Cart merge process completed (or skipped).');
      // ================================================
    } catch (mergeError) {
      // Lỗi merge không nên ngăn cản việc đăng nhập, chỉ log lại
      console.error("DEBUG: LoginView - Error during cart merge:", mergeError);
    }

    // --- Logic điều hướng ĐÃ CẬP NHẬT ---
    const redirectPath = authStore.returnUrl;
    authStore.setReturnUrl(null); // Xóa returnUrl ngay
    console.log(`DEBUG: LoginView - redirectPath from store: ${redirectPath}`);

    let finalRedirect = null;
    // Ưu tiên returnUrl nếu có
    if (redirectPath) {
      finalRedirect = redirectPath;
    } else {
      // <<< KIỂM TRA ROLE ĐỂ ĐIỀU HƯỚNG >>>
      finalRedirect = authStore.isAdmin ? {name: 'adminDashboard'} : {name: 'home'};
    }

    console.log('DEBUG: LoginView - Determined final redirect:', finalRedirect);
    try {
      await router.push(finalRedirect); // Chuyển hướng
      console.log('DEBUG: LoginView - Navigation successful.');
    } catch (navError) {
      console.error('DEBUG: LoginView - Navigation failed:', navError);
    }
    // -----------------------------------
  }
  // Nếu login không thành công (success = false), lỗi đã được set trong authStore
};
</script>

<style scoped>
.login-view {
  background: url('https://goldviet24k.vn/pic/Product/images/c%C3%B3c%20ng%E1%BA%ADm%20ti%E1%BB%81n%20v%C3%A0ng%20%C4%91%E1%BB%93ng%201d.jpg') no-repeat center center;
  background-size: cover;
}
</style>
