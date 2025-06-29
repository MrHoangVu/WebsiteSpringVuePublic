<template>
  <div class="admin-user-detail-view">
    <!-- Back Button -->
    <div class="mb-3">
      <router-link :to="{ name: 'adminUserList' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Danh sách KH
      </router-link>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-5"> ... </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger"> ... </div>

    <!-- User Detail Content -->
    <div v-else-if="user" class="user-detail-content">
      <h1 class="mb-4">Chi tiết Khách hàng: <strong class="text-primary">{{ user.username }}</strong></h1>

      <div class="row g-4">
        <!-- Left Column: User Info, Tier, Status -->
        <div class="col-lg-5">
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Thông tin cơ bản</strong></div>
            <div class="card-body">
              <dl class="row mb-0">
                <dt class="col-sm-5">Tên đăng nhập:</dt><dd class="col-sm-7">{{ user.username }}</dd>
                <dt class="col-sm-5">Họ và Tên:</dt><dd class="col-sm-7">{{ user.fullName || 'N/A' }}</dd>
                <dt class="col-sm-5">Ngày tham gia:</dt><dd class="col-sm-7">{{ formatDate(user.createdAt) }}</dd>
                <dt class="col-sm-5">Tổng chi tiêu:</dt><dd class="col-sm-7 fw-bold text-danger">{{ formatCurrency(user.totalSpent) }}</dd>
                <dt class="col-sm-5">Trạng thái TK:</dt>
                <dd class="col-sm-7">
                   <span class="badge rounded-pill" :class="user.isActive ? 'text-bg-success' : 'text-bg-secondary'">
                     {{ user.isActive ? 'Hoạt động' : 'Đã khóa' }}
                   </span>
                </dd>
              </dl>
            </div>
          </div>

          <!-- Tier & Status Update Card -->
          <div class="card shadow-sm">
            <div class="card-header bg-warning"><strong>Quản lý Khách hàng</strong></div>
            <div class="card-body">
              <!-- Tier Update -->
              <div class="mb-3">
                <label for="customerTier" class="form-label">Bậc khách hàng:</label>
                <div class="input-group">
                  <select class="form-select" id="customerTier" v-model="selectedTier" :disabled="updatingTier">
                    <option value="BRONZE">Đồng</option>
                    <option value="SILVER">Bạc</option>
                    <option value="GOLD">Vàng</option>
                    <option value="DIAMOND">Kim cương</option>
                  </select>
                  <button class="btn btn-primary" @click="handleUpdateTier" :disabled="updatingTier || selectedTier === user.tier">
                    <span v-if="updatingTier" class="spinner-border spinner-border-sm me-1"></span> Lưu Bậc
                  </button>
                </div>
                <div v-if="tierUpdateError" class="text-danger small mt-1">{{ tierUpdateError }}</div>
                <div v-if="tierUpdateSuccess" class="text-success small mt-1">Cập nhật bậc thành công!</div>
              </div>

              <!-- Status Update -->
              <div>
                <label class="form-label">Trạng thái tài khoản:</label>
                <button class="btn w-100"
                        :class="user.isActive ? 'btn-outline-warning' : 'btn-outline-success'"
                        @click="handleUpdateStatus"
                        :disabled="updatingStatus">
                  <span v-if="updatingStatus" class="spinner-border spinner-border-sm me-1"></span>
                  {{ user.isActive ? 'Khóa Tài Khoản' : 'Mở Khóa Tài Khoản' }}
                </button>
                <div v-if="statusUpdateError" class="text-danger small mt-1">{{ statusUpdateError }}</div>
                <div v-if="statusUpdateSuccess" class="text-success small mt-1">Cập nhật trạng thái thành công!</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Addresses & Recent Orders -->
        <div class="col-lg-7">
          <!-- Addresses Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Địa chỉ đã lưu ({{ user.addresses?.length || 0 }})</strong></div>
            <div class="card-body" v-if="user.addresses && user.addresses.length > 0">
              <ul class="list-group list-group-flush">
                <li v-for="addr in user.addresses" :key="addr.id" class="list-group-item px-0">
                  <strong>{{ addr.recipientName }}</strong> - {{ addr.recipientPhone }}
                  <span v-if="addr.isDefaultShipping" class="badge bg-secondary ms-1">Giao hàng mặc định</span>
                  <br>
                  <small class="text-muted">
                    {{ addr.streetAddress }}, {{ addr.ward ? addr.ward + ', ' : '' }}{{ addr.district }}, {{ addr.city }}
                  </small>
                </li>
              </ul>
            </div>
            <div class="card-body text-muted text-center" v-else>
              Khách hàng chưa lưu địa chỉ nào.
            </div>
          </div>

          <!-- Recent Orders Card -->
          <div class="card shadow-sm">
            <div class="card-header bg-light"><strong>Đơn hàng gần đây (Tối đa 5)</strong></div>
            <div class="card-body p-0" v-if="user.recentOrders && user.recentOrders.length > 0">
              <ul class="list-group list-group-flush">
                <li v-for="order in user.recentOrders" :key="order.orderId" class="list-group-item px-3 py-2">
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <router-link :to="{ name: 'adminOrderDetail', params: { orderId: order.orderId } }" class="fw-medium">#{{ order.orderId }}</router-link>
                      <small class="d-block text-muted">{{ formatDate(order.orderDate) }}</small>
                    </div>
                    <div>
                      <span class="badge me-2" :class="getStatusClass(order.status)">{{ formatStatus(order.status) }}</span>
                      <span class="fw-bold">{{ formatCurrency(order.totalAmount) }}</span>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
            <div class="card-body text-muted text-center" v-else>
              Khách hàng chưa có đơn hàng nào.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { getAdminCustomerDetail, updateAdminCustomerTier, updateAdminCustomerStatus } from '@/http/modules/admin/adminUserService.js';
import { formatCurrency, formatDate, formatStatus, getStatusClass } from '@/utils/formatters'; // Import helpers

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  }
});

const route = useRoute();
const router = useRouter();

const user = ref(null);
const loading = ref(true);
const error = ref(null);

// State for Tier update
const selectedTier = ref('');
const updatingTier = ref(false);
const tierUpdateError = ref(null);
const tierUpdateSuccess = ref(false);

// State for Status update
const updatingStatus = ref(false);
const statusUpdateError = ref(null);
const statusUpdateSuccess = ref(false);


const fetchUserDetail = async () => {
  loading.value = true;
  error.value = null;
  user.value = null;
  resetUpdateStates(); // Reset thông báo khi load lại
  try {
    const response = await getAdminCustomerDetail(props.userId);
    user.value = response.data;
    selectedTier.value = user.value?.tier || 'BRONZE'; // Set initial tier for dropdown
  } catch (err) {
    console.error(`Error fetching user detail for ID ${props.userId}:`, err);
    error.value = err.response?.data?.message || "Không thể tải chi tiết khách hàng.";
  } finally {
    loading.value = false;
  }
};

// --- Update Handlers ---
const handleUpdateTier = async () => {
  if (!user.value || selectedTier.value === user.value.tier) return;
  updatingTier.value = true;
  tierUpdateError.value = null;
  tierUpdateSuccess.value = false;
  try {
    const response = await updateAdminCustomerTier(user.value.id, selectedTier.value);
    // Cập nhật lại tier trong user ref
    if (user.value) { // Check again in case user becomes null somehow
      user.value.tier = response.data.tier; // Giả sử API trả về DTO có tier mới
    }
    tierUpdateSuccess.value = true;
    setTimeout(() => tierUpdateSuccess.value = false, 3000); // Hide success message
  } catch (err) {
    console.error(`Error updating tier for user ${user.value.id}:`, err);
    tierUpdateError.value = err.response?.data?.message || "Lỗi cập nhật bậc khách hàng.";
    setTimeout(() => tierUpdateError.value = null, 5000); // Hide error message
  } finally {
    updatingTier.value = false;
  }
};

const handleUpdateStatus = async () => {
  if (!user.value) return;
  const newStatus = !user.value.isActive;
  const actionText = newStatus ? 'mở khóa' : 'khóa';
  if (!confirm(`Bạn có chắc muốn ${actionText} tài khoản "${user.value.username}"?`)) return;

  updatingStatus.value = true;
  statusUpdateError.value = null;
  statusUpdateSuccess.value = false;
  try {
    const response = await updateAdminCustomerStatus(user.value.id, newStatus);
    if (user.value) {
      user.value.isActive = response.data.isActive; // Cập nhật trạng thái
    }
    statusUpdateSuccess.value = true;
    setTimeout(() => statusUpdateSuccess.value = false, 3000);
  } catch (err) {
    console.error(`Error updating status for user ${user.value.id}:`, err);
    statusUpdateError.value = err.response?.data?.message || `Lỗi khi ${actionText} tài khoản.`;
    setTimeout(() => statusUpdateError.value = null, 5000);
  } finally {
    updatingStatus.value = false;
  }
};

const resetUpdateStates = () => {
  updatingTier.value = false; tierUpdateError.value = null; tierUpdateSuccess.value = false;
  updatingStatus.value = false; statusUpdateError.value = null; statusUpdateSuccess.value = false;
};

// --- Watchers & Lifecycle ---
watch(() => props.userId, fetchUserDetail, { immediate: true });

</script>

<style scoped>
.admin-user-detail-view dt { font-weight: 500; color: #6c757d; }
.admin-user-detail-view dd { margin-bottom: 0.5rem; }
.list-group-item small { display: block; }
.list-group-item .badge { font-size: 0.8em; }
</style>
