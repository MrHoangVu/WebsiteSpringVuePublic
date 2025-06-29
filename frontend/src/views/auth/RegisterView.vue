// src/views/RegisterView.vue

<template>
  <div class="register-view d-flex align-items-center justify-content-center min-vh-100 bg-light">
    <div class="card shadow-sm" style="width: 100%; max-width: 450px;">
      <div class="card-body p-4 p-lg-5">
        <h2 class="card-title text-center mb-4">Đăng Ký Tài Khoản</h2>
        <form @submit.prevent="handleRegister" novalidate>

          <!-- Thông báo lỗi chung từ API -->
          <div v-if="error" class="alert alert-danger" role="alert">
            {{ error }}
          </div>
          <!-- Thông báo thành công -->
          <div v-if="successMessage" class="alert alert-success" role="alert">
            {{ successMessage }} Đang chuyển hướng đến trang đăng nhập...
            {/* <RouterLink :to="{name: 'login'}">Đăng nhập ngay</RouterLink>. */}
          </div>

          <!-- Username -->
          <div class="mb-3">
            <label for="username" class="form-label">Tên đăng nhập <span class="text-danger">*</span></label>
            <input
              type="text"
              class="form-control"
              :class="{ 'is-invalid': validationErrors.username }"
              id="username"
              v-model.trim="formData.username"
            required
            placeholder="Ít nhất 3 ký tự"
            :disabled="loading || !!successMessage"
            aria-describedby="usernameFeedback"
            />
            <div id="usernameFeedback" v-if="validationErrors.username" class="invalid-feedback">
              {{ validationErrors.username }}
            </div>
          </div>

          <!-- Full Name (Tùy chọn) -->
          <div class="mb-3">
            <label for="fullName" class="form-label">Họ và Tên</label>
            <input
              type="text"
              class="form-control"
              :class="{ 'is-invalid': validationErrors.fullName }"
              id="fullName"
              v-model.trim="formData.fullName"
            placeholder="Ví dụ: Nguyễn Văn A"
            :disabled="loading || !!successMessage"
            aria-describedby="fullNameFeedback"
            />
            <div id="fullNameFeedback" v-if="validationErrors.fullName" class="invalid-feedback">
              {{ validationErrors.fullName }}
            </div>
          </div>

          <!-- Email (Optional - Uncomment if needed) -->
          <!--
          <div class="mb-3">
            <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
            <input type="email" class="form-control" :class="{ 'is-invalid': validationErrors.email }" id="email" v-model.trim="formData.email" required placeholder="vidu@email.com" :disabled="loading || !!successMessage" aria-describedby="emailFeedback"/>
            <div id="emailFeedback" v-if="validationErrors.email" class="invalid-feedback">{{ validationErrors.email }}</div>
          </div>
          -->

          <!-- Password -->
          <div class="mb-3">
            <label for="password" class="form-label">Mật khẩu <span class="text-danger">*</span></label>
            <input
              type="password"
              class="form-control"
              :class="{ 'is-invalid': validationErrors.password }"
              id="password"
              v-model="formData.password"
              required
              placeholder="Ít nhất 6 ký tự"
              :disabled="loading || !!successMessage"
              aria-describedby="passwordFeedback"
            />
            <div id="passwordFeedback" v-if="validationErrors.password" class="invalid-feedback">
              {{ validationErrors.password }}
            </div>
          </div>

          <!-- Confirm Password -->
          <div class="mb-3">
            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu <span class="text-danger">*</span></label>
            <input
              type="password"
              class="form-control"
              :class="{ 'is-invalid': validationErrors.confirmPassword }"
              id="confirmPassword"
              v-model="formData.confirmPassword"
              required
              placeholder="Nhập lại mật khẩu"
              :disabled="loading || !!successMessage"
              aria-describedby="confirmPasswordFeedback"
            />
            <div id="confirmPasswordFeedback" v-if="validationErrors.confirmPassword" class="invalid-feedback">
              {{ validationErrors.confirmPassword }}
            </div>
          </div>

          <!-- Nút Submit -->
          <div class="d-grid">

            <button type="submit" class="btn btn-primary btn-lg" :disabled="loading || !!successMessage">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              {{ loading ? 'Đang xử lý...' : 'Đăng ký' }}
            </button>
          </div>
        </form>
        <div class="text-center mt-3 small">

          Bạn đã có tài khoản? <RouterLink :to="{name: 'login'}">Đăng nhập</RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { RouterLink, useRouter } from 'vue-router'; // Import useRouter
import apiClient from '@/http/axios.js'; // Ensure this path is correct

// --- State ---
const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  // email: '' // Uncomment if using email field
});
const validationErrors = reactive({}); // Stores client-side validation errors
const loading = ref(false);
const error = ref(null); // Stores API error messages
const successMessage = ref(null); // Stores API success message
const router = useRouter(); // Initialize router for navigation

// --- Client-Side Validation ---
const validateForm = () => {
  // Clear previous errors
  Object.keys(validationErrors).forEach(key => delete validationErrors[key]);
  let isValid = true;

  // Username validation
  if (!formData.username || formData.username.length < 3 || formData.username.length > 50) {
    validationErrors.username = 'Tên đăng nhập phải từ 3 đến 50 ký tự.';
    isValid = false;
  } else if (/\s/.test(formData.username)) { // Check for spaces
    validationErrors.username = 'Tên đăng nhập không được chứa khoảng trắng.';
    isValid = false;
  }

  // Full Name validation (optional, only check length if provided)
  if (formData.fullName && formData.fullName.length > 150) {
    validationErrors.fullName = 'Họ tên không được quá 150 ký tự.';
    isValid = false;
  }

  // Password validation
  if (!formData.password || formData.password.length < 6 || formData.password.length > 100) {
    validationErrors.password = 'Mật khẩu phải từ 6 đến 100 ký tự.';
    isValid = false;
  }

  // Confirm Password validation
  if (!formData.confirmPassword) {
    validationErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu.';
    isValid = false;
  } else if (formData.password && formData.password !== formData.confirmPassword) {
    validationErrors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    isValid = false;
  }

  // Email validation (Uncomment if using email field)
  /*
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formData.email) {
      validationErrors.email = 'Vui lòng nhập địa chỉ email.';
      isValid = false;
  } else if (!emailRegex.test(formData.email)) {
      validationErrors.email = 'Định dạng email không hợp lệ.';
      isValid = false;
  }
  */

  return isValid;
};

// --- Registration Handler ---
const handleRegister = async () => {
  // Clear previous API error and success messages
  error.value = null;
  successMessage.value = null;

  // Perform client-side validation first
  if (!validateForm()) {
    console.log('DEBUG: Client-side validation failed.', validationErrors);
    return; // Stop if validation fails
  }

  loading.value = true; // Disable button and show spinner

  try {
    // Prepare payload for API (exclude confirmPassword)
    const payload = {
      username: formData.username,
      password: formData.password,
      fullName: formData.fullName || null, // Send null if empty
      // email: formData.email // Uncomment if using email field
    };
    console.log('DEBUG: Sending registration payload:', payload);

    // Make API call
    const response = await apiClient.post('/auth/register', payload);
    console.log('DEBUG: Registration API response:', response);

    // --- Success Handling ---
    successMessage.value = response.data.message || 'Đăng ký thành công!';

    // Reset form fields
    Object.keys(formData).forEach(key => { formData[key] = ''; });
    // Clear any remaining client validation errors (shouldn't be any, but good practice)
    Object.keys(validationErrors).forEach(key => delete validationErrors[key]);

    console.log('DEBUG: Register successful. Redirecting to login in 2 seconds...');

    // Redirect to login page after a short delay
    setTimeout(() => {
      try {
        console.log('DEBUG: Executing redirect to login page');
        router.push({ name: 'login' }); // Use the correct route name 'login'
      } catch(navError) {
        console.error('DEBUG: Failed to redirect after registration:', navError);
        // Show error, but keep success message so user knows registration worked
        error.value = "Đăng ký thành công nhưng lỗi chuyển trang. Vui lòng đăng nhập thủ công.";
        // Keep loading false, button remains disabled due to successMessage
      }
    }, 2000); // 2-second delay

  } catch (err) {
    // --- Error Handling ---
    console.error("Registration API failed:", err);
    if (err.response) {
      console.error("Error Response Data:", err.response.data);
      console.error("Error Response Status:", err.response.status);
      // Prefer specific error message from backend if available
      if (err.response.data && (err.response.data.error || err.response.data.message)) {
        error.value = err.response.data.error || err.response.data.message;
      } else if (err.response.status === 400) {
        error.value = "Dữ liệu gửi đi không hợp lệ. Vui lòng kiểm tra lại các trường đã nhập.";
      } else if (err.response.status === 409) { // Conflict (e.g., username exists)
        error.value = "Tên đăng nhập hoặc email đã tồn tại."; // Be more specific if backend tells you which
      }
      else {
        error.value = `Lỗi từ máy chủ (mã ${err.response.status}). Vui lòng thử lại.`;
      }
    } else if (err.request) {
      // Network error (no response received)
      console.error("Network Error:", err.request);
      error.value = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng và thử lại.';
    } else {
      // Other errors (e.g., setup error)
      console.error('Error', err.message);
      error.value = 'Đã có lỗi không xác định xảy ra. Vui lòng thử lại.';
    }
  } finally {
    // Loading should only be turned off if there was an error AND no success message.
    // If successful, the button stays disabled via `!!successMessage` until redirect.
    if (error.value && !successMessage.value) {
      loading.value = false;
    }
    // If only success, loading remains true (or irrelevant as button is disabled by successMessage)
    // If only error, loading becomes false.
  }
};
</script>

<style scoped>
.register-view {
  background: url('https://goldviet24k.vn/pic/Product/images/c%C3%B3c%20ng%E1%BA%ADm%20ti%E1%BB%81n%20v%C3%A0ng%20%C4%91%E1%BB%93ng%201d.jpg') no-repeat center center;
  background-size: cover;
}

.card {
  border: none; /* Remove default border if using shadow */
}
/* Ensure Bootstrap's invalid feedback is always potentially visible */
.invalid-feedback {
  display: block;
  min-height: 1.2em; /* Reserve space to prevent layout shifts */
  /* visibility: hidden; You could hide/show with visibility if preferred */
}
.form-control.is-invalid ~ .invalid-feedback {
  /* visibility: visible; */
}
.text-danger {
  font-size: 0.9em; /* Slightly smaller asterisk */
}

</style>
