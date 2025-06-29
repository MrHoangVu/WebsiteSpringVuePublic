<template>
  <div class="user-profile-view container mt-4 mb-5">
    <h1 class="mb-4">Thông Tin Tài Khoản</h1>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger">Lỗi tải thông tin tài khoản: {{ error }}</div>

    <!-- Profile Content -->
    <div v-else-if="userProfile" class="row g-4">
      <!-- Left Column: Profile Info & Update Form -->
      <div class="col-lg-7">
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-light"><strong>Thông tin cá nhân</strong></div>
          <div class="card-body">
            <!-- Display Info -->
            <dl class="row mb-4">
              <dt class="col-sm-4">Tên đăng nhập:</dt>
              <dd class="col-sm-8">{{ userProfile.username }}</dd>

              <dt class="col-sm-4">Họ và Tên:</dt>
              <dd class="col-sm-8">{{ userProfile.fullName || "Chưa cập nhật" }}</dd>

              <!-- <dt class="col-sm-4">Email:</dt>
              <dd class="col-sm-8">{{ userProfile.email || 'Chưa cập nhật' }}</dd> -->

              <dt class="col-sm-4">Ngày tham gia:</dt>
              <dd class="col-sm-8">{{ formatDate(userProfile.createdAt) }}</dd>

              <dt class="col-sm-4">Hạng thành viên:</dt>
              <dd class="col-sm-8">
                <span class="badge fs-6" :class="getTierClass(userProfile.tier)">
                  {{ formatTier(userProfile.tier) }}
                </span>
              </dd>

              <dt class="col-sm-4">Tổng chi tiêu:</dt>
              <dd class="col-sm-8 fw-bold text-danger">
                {{ formatCurrency(userProfile.totalSpent) }}
              </dd>
            </dl>

            <!-- Update Profile Form -->
            <hr />
            <h5 class="h6 mb-3">Cập nhật thông tin</h5>
            <!-- Update Success/Error Messages -->
            <div v-if="updateProfileError" class="alert alert-danger small p-2">
              {{ updateProfileError }}
            </div>
            <div v-if="updateProfileSuccess" class="alert alert-success small p-2">
              Cập nhật thông tin thành công!
            </div>

            <form @submit.prevent="handleUpdateProfile">
              <div class="mb-3">
                <label for="profileFullName" class="form-label">Họ và Tên</label>
                <input
                  type="text"
                  class="form-control"
                  id="profileFullName"
                  v-model="updateFormData.fullName"
                  :disabled="updatingProfile"
                />
              </div>
              <!-- Add other updatable fields like email here -->
              <button type="submit" class="btn btn-primary" :disabled="updatingProfile">
                <span v-if="updatingProfile" class="spinner-border spinner-border-sm me-1"></span>
                Lưu thay đổi
              </button>
            </form>
          </div>
        </div>

        <!-- Change Password Card -->
        <div class="card shadow-sm">
          <div class="card-header bg-light"><strong>Đổi mật khẩu</strong></div>
          <div class="card-body">
            <!-- Change Password Success/Error Messages -->
            <div v-if="changePasswordError" class="alert alert-danger small p-2">
              {{ changePasswordError }}
            </div>
            <div v-if="changePasswordSuccess" class="alert alert-success small p-2">
              Đổi mật khẩu thành công!
            </div>

            <form @submit.prevent="handleChangePassword">
              <div class="mb-3">
                <label for="currentPassword" class="form-label">Mật khẩu hiện tại</label>
                <input
                  type="password"
                  class="form-control"
                  id="currentPassword"
                  v-model="passwordFormData.currentPassword"
                  required
                  :disabled="changingPassword"
                />
              </div>
              <div class="mb-3">
                <label for="newPassword" class="form-label">Mật khẩu mới</label>
                <input
                  type="password"
                  class="form-control"
                  id="newPassword"
                  v-model="passwordFormData.newPassword"
                  required
                  :disabled="changingPassword"
                />
              </div>
              <div class="mb-3">
                <label for="confirmNewPassword" class="form-label">Xác nhận mật khẩu mới</label>
                <input
                  type="password"
                  class="form-control"
                  :class="{ 'is-invalid': passwordMismatch }"
                  id="confirmNewPassword"
                  v-model="passwordFormData.confirmNewPassword"
                  required
                  :disabled="changingPassword"
                />
                <div v-if="passwordMismatch" class="invalid-feedback">
                  Mật khẩu xác nhận không khớp.
                </div>
              </div>
              <button
                type="submit"
                class="btn btn-secondary"
                :disabled="changingPassword || passwordMismatch"
              >
                <span v-if="changingPassword" class="spinner-border spinner-border-sm me-1"></span>
                Đổi mật khẩu
              </button>
            </form>
          </div>
        </div>
      </div>

      <!-- Right Column: Order History / Addresses (Optional) -->
      <div class="col-lg-5">
        <!-- Recent Orders -->
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-light d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Đơn hàng gần đây</h5>
            <router-link :to="{ name: 'orderHistory' }" class="btn btn-sm btn-outline-primary"
              >Xem tất cả</router-link
            >
          </div>
          <div class="card-body p-0">
            <div v-if="loadingOrders" class="text-center p-3">
              <div class="spinner-border spinner-border-sm"></div>
            </div>
            <div v-else-if="errorOrders" class="alert alert-warning small m-3">
              {{ errorOrders }}
            </div>
            <ul v-else-if="recentOrders.length > 0" class="list-group list-group-flush">
              <li
                v-for="order in recentOrders"
                :key="order.orderId"
                class="list-group-item px-3 py-2"
              >
                <div class="d-flex justify-content-between">
                  <router-link
                    :to="{ name: 'userOrderDetail', params: { orderId: order.orderId } }"
                    class="fw-medium"
                    >#{{ order.orderId }}</router-link
                  >
                  <span class="fw-bold">{{ formatCurrency(order.totalAmount) }}</span>
                </div>
                <small class="text-muted d-block"
                  >{{ formatDate(order.orderDate) }} -
                  <span :class="getStatusClass(order.status)">{{
                    formatStatus(order.status)
                  }}</span></small
                >
              </li>
            </ul>
            <div v-else class="text-muted text-center p-3">Bạn chưa có đơn hàng nào.</div>
          </div>
        </div>

        <!-- Address Management (Optional - Requires separate implementation) -->
        <!--
        <div class="card shadow-sm">
          <div class="card-header bg-light"><strong>Sổ địa chỉ</strong></div>
          <div class="card-body">
             <p>Quản lý địa chỉ giao hàng của bạn.</p>
             <button class="btn btn-outline-secondary">Quản lý địa chỉ</button>
          </div>
        </div>
        -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth.js";
// Import API services
import {
  getUserProfile,
  updateUserProfile,
  changeUserPassword,
} from "@/http/modules/public/userService.js"; // Cần tạo userService.js
import { getUserOrders } from "@/http/modules/public/orderService.js";
// Import formatters
import {
  formatCurrency,
  formatDate,
  formatTier,
  getTierClass,
  formatStatus,
  getStatusClass,
} from "@/utils/formatters.js";

const authStore = useAuthStore();
const router = useRouter();

// State for profile data
const userProfile = ref(null);
const loading = ref(true);
const error = ref(null);

// State for update profile form
const updateFormData = reactive({
  fullName: "",
  // email: '' // Add if needed
});
const updatingProfile = ref(false);
const updateProfileError = ref(null);
const updateProfileSuccess = ref(false);

// State for change password form
const passwordFormData = reactive({
  currentPassword: "",
  newPassword: "",
  confirmNewPassword: "",
});
const changingPassword = ref(false);
const changePasswordError = ref(null);
const changePasswordSuccess = ref(false);
const passwordMismatch = computed(
  () =>
    passwordFormData.newPassword &&
    passwordFormData.confirmNewPassword &&
    passwordFormData.newPassword !== passwordFormData.confirmNewPassword
);

// State for recent orders
const recentOrders = ref([]);
const loadingOrders = ref(false);
const errorOrders = ref(null);

// Fetch User Profile Data
const fetchUserProfile = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await getUserProfile(); // Gọi API GET /api/users/me
    userProfile.value = response.data;
    // Initialize update form data
    updateFormData.fullName = userProfile.value?.fullName || "";
    // updateFormData.email = userProfile.value?.email || '';
  } catch (err) {
    console.error("Error fetching user profile:", err);
    error.value = err.response?.data?.message || "Không thể tải thông tin tài khoản.";
    // Handle unauthorized error specifically if needed
    if (err.response?.status === 401 || err.response?.status === 403) {
      // Redirect to login or show specific message
      // authStore.logout(); // Optional: Force logout
      // router.push({ name: 'login' });
    }
  } finally {
    loading.value = false;
  }
};

// Fetch Recent Orders
const fetchRecentOrders = async () => {
  loadingOrders.value = true;
  errorOrders.value = null;
  try {
    const params = { page: 0, size: 5, sort: "orderDate,desc" }; // Lấy 5 đơn gần nhất
    const response = await getUserOrders(params);
    recentOrders.value = response.data?.content || [];
  } catch (err) {
    console.error("Error fetching recent orders:", err);
    errorOrders.value = "Lỗi tải đơn hàng gần đây.";
  } finally {
    loadingOrders.value = false;
  }
};

// Handle Update Profile
const handleUpdateProfile = async () => {
  updatingProfile.value = true;
  updateProfileError.value = null;
  updateProfileSuccess.value = false;
  try {
    const payload = { fullName: updateFormData.fullName };
    // Add other fields to payload if needed
    const response = await updateUserProfile(payload); // Gọi API PUT /api/users/me
    userProfile.value = response.data; // Cập nhật profile trên UI
    updateProfileSuccess.value = true;
    setTimeout(() => (updateProfileSuccess.value = false), 3000); // Hide success message
  } catch (err) {
    console.error("Error updating profile:", err);
    updateProfileError.value = err.response?.data?.message || "Cập nhật thông tin thất bại.";
    setTimeout(() => (updateProfileError.value = null), 5000); // Hide error message
  } finally {
    updatingProfile.value = false;
  }
};

// Handle Change Password
const handleChangePassword = async () => {
  if (passwordMismatch.value) return;
  changingPassword.value = true;
  changePasswordError.value = null;
  changePasswordSuccess.value = false;
  try {
    const payload = { ...passwordFormData };
    await changeUserPassword(payload); // Gọi API PUT /api/users/me/password
    changePasswordSuccess.value = true;
    // Reset password form
    passwordFormData.currentPassword = "";
    passwordFormData.newPassword = "";
    passwordFormData.confirmNewPassword = "";
    setTimeout(() => (changePasswordSuccess.value = false), 3000);
  } catch (err) {
    console.error("Error changing password:", err);
    changePasswordError.value = err.response?.data?.message || "Đổi mật khẩu thất bại.";
    setTimeout(() => (changePasswordError.value = null), 5000);
  } finally {
    changingPassword.value = false;
  }
};

// Fetch data on mount
onMounted(() => {
  if (authStore.isAuthenticated) {
    fetchUserProfile();
    fetchRecentOrders();
  } else {
    // Should be handled by route guard, but as a fallback:
    router.push({ name: "login" });
  }
});
</script>

<style scoped>
.user-profile-view {
  min-height: 70vh;
}
.card-header {
  font-weight: 500;
}
dt {
  font-weight: 500;
  color: #6c757d;
}
dd {
  margin-bottom: 0.75rem;
}
.badge.fs-6 {
  padding: 0.4em 0.8em;
}
.is-invalid ~ .invalid-feedback {
  display: block;
}
</style>
