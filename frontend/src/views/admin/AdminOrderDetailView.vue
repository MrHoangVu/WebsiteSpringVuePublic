<template>
  <div class="admin-order-detail-view container mt-4 mb-5">

    <!-- Back Button -->
    <div class="mb-3">
      <router-link :to="{ name: 'adminOrderList' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Danh sách
      </router-link>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải chi tiết đơn hàng...</span>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger">
      Lỗi tải chi tiết đơn hàng: {{ error }}
    </div>

    <!-- Order Detail Content -->
    <div v-else-if="order" class="order-detail-content">
      <h1 class="mb-4">Chi tiết Đơn hàng <strong class="text-primary">#{{ order.orderId }}</strong></h1>

      <div class="row g-4">
        <!-- Left Column: Order Info, Customer, Shipping -->
        <div class="col-lg-7">
          <!-- General Info Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Thông tin chung</strong></div>
            <div class="card-body">
              <dl class="row mb-0">
                <dt class="col-sm-4">Ngày đặt:</dt>
                <dd class="col-sm-8">{{ formatDateTime(order.orderDate) }}</dd>

                <dt class="col-sm-4">Trạng thái:</dt>
                <dd class="col-sm-8">
                  <span class="badge rounded-pill fs-6" :class="getStatusClass(order.status)">
                    {{ formatStatus(order.status) }}
                  </span>
                </dd>

                <dt class="col-sm-4">Thanh toán:</dt>
                <dd class="col-sm-8">{{ order.paymentMethod }}</dd>

                <dt class="col-sm-4" v-if="order.orderNote">Ghi chú KH:</dt>
                <dd class="col-sm-8" v-if="order.orderNote" style="white-space: pre-wrap;">{{ order.orderNote }}</dd>
              </dl>
            </div>
          </div>

          <!-- Customer/Recipient Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Thông tin Khách hàng & Giao hàng</strong></div>
            <div class="card-body">
              <dl class="row mb-0">
                <dt class="col-sm-4">Khách hàng:</dt>
                <dd class="col-sm-8">
                  <span v-if="order.username">{{ order.username }} (ID: {{ order.userId }})</span>
                  <span v-else-if="order.guestEmail">{{ order.guestEmail }} (Guest)</span>
                  <span v-else>(Không xác định)</span>
                </dd>

                <dt class="col-sm-4">Người nhận:</dt>
                <dd class="col-sm-8">{{ order.shippingRecipientName }}</dd>

                <dt class="col-sm-4">Điện thoại:</dt>
                <dd class="col-sm-8">{{ order.shippingRecipientPhone }}</dd>

                <dt class="col-sm-4">Địa chỉ:</dt>
                <dd class="col-sm-8">
                  {{ order.shippingStreetAddress }}, {{ order.shippingWard ? order.shippingWard + ', ' : '' }}{{ order.shippingDistrict }}, {{ order.shippingCity }}
                </dd>

                <dt class="col-sm-4">Vận chuyển:</dt>
                <dd class="col-sm-8">{{ order.shippingMethodName || 'N/A' }} ({{ formatCurrency(order.shippingCost) }})</dd>

                <dt class="col-sm-4" v-if="order.promotionCode">Khuyến mãi:</dt>
                <dd class="col-sm-8 text-success" v-if="order.promotionCode">
                  {{ order.promotionCode }} (-{{ formatCurrency(order.discountAmount) }})
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <!-- Right Column: Items & Actions -->
        <div class="col-lg-5">
          <!-- Order Items Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Sản phẩm đã đặt</strong></div>
            <div class="card-body p-0">
              <ul class="list-group list-group-flush">
                <li v-for="item in order.items" :key="item.productId" class="list-group-item d-flex align-items-center">
                  <img :src="item.productImageUrl || defaultImage" :alt="item.productName" width="50" class="me-3 rounded border">
                  <div class="flex-grow-1">
                    <div class="fw-medium">{{ item.productName }}</div>
                    <small class="text-muted">SL: {{ item.quantity }} x {{ formatCurrency(item.priceAtPurchase) }}</small>
                  </div>
                  <span class="fw-bold ms-2">{{ formatCurrency(item.quantity * item.priceAtPurchase) }}</span>
                </li>
              </ul>
            </div>
            <div class="card-footer bg-light d-flex justify-content-between fw-bold fs-5">
              <span>Tổng cộng:</span>
              <span class="text-danger">{{ formatCurrency(order.totalAmount) }}</span>
            </div>
          </div>

          <!-- Admin Actions Card -->
          <div class="card shadow-sm">
            <div class="card-header bg-warning"><strong>Cập nhật trạng thái</strong></div>
            <div class="card-body">
              <div v-if="!canUpdateStatus" class="alert alert-secondary small p-2 text-center">
                Đơn hàng ở trạng thái "{{ formatStatus(order.status) }}" không thể thay đổi.
              </div>
              <div v-else>
                <label for="orderStatusSelect" class="form-label">Chọn trạng thái mới:</label>
                <select
                  class="form-select"
                  id="orderStatusSelect"
                  v-model="selectedNewStatus"
                  :disabled="updatingStatus"
                >
                  <option disabled value="">-- Chọn trạng thái --</option>
                  <!-- Chỉ hiển thị các trạng thái hợp lệ để chuyển tới -->
                  <option v-for="status in availableNextStatuses" :key="status" :value="status">
                    {{ formatStatus(status) }}
                  </option>
                </select>

                <!-- Update Status Error/Info -->
                <div v-if="updateError" class="alert alert-danger small p-2 mt-2">{{ updateError }}</div>
                <div v-if="updateSuccess" class="alert alert-success small p-2 mt-2">Cập nhật trạng thái thành công!</div>

                <button
                  class="btn btn-primary w-100 mt-3"
                  @click="handleUpdateStatus"
                  :disabled="updatingStatus || !selectedNewStatus"
                >
                  <span v-if="updatingStatus" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ updatingStatus ? 'Đang cập nhật...' : 'Lưu thay đổi' }}
                </button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Order Not Found Fallback -->
    <div v-else-if="!loading && !order && !error" class="alert alert-warning text-center">
      Không tìm thấy đơn hàng với ID này.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { getAdminOrderDetail, updateOrderStatus } from '@/http/modules/admin/adminOrderService.js';
import defaultImage from '@/assets/images/placeholder.png';

const props = defineProps({
  orderId: {
    type: [String, Number],
    required: true
  }
});

const route = useRoute();
const router = useRouter();

// Order Data State
const order = ref(null);
const loading = ref(true);
const error = ref(null);

// Status Update State
const selectedNewStatus = ref('');
const updatingStatus = ref(false);
const updateError = ref(null);
const updateSuccess = ref(false);

// Define all possible statuses and their display names
const allStatuses = {
  'PENDING': 'Chờ xác nhận',
  'PROCESSING': 'Đang xử lý',
  'SHIPPING': 'Đang giao hàng',
  'COMPLETED': 'Đã hoàn thành',
  'CANCELLED': 'Đã hủy',
  'PAYMENT_PENDING': 'Chờ thanh toán',
};

// Define valid transitions (Backend should enforce this too!)
const validTransitions = {
  'PENDING': ['PROCESSING', 'CANCELLED'],
  'PAYMENT_PENDING': ['PROCESSING', 'CANCELLED'], // Same as PENDING?
  'PROCESSING': ['SHIPPING', 'CANCELLED'],
  'SHIPPING': ['COMPLETED', 'CANCELLED'], // Consider CANCELLED carefully here
  'COMPLETED': [], // Cannot change from final state
  'CANCELLED': [], // Cannot change from final state
};

// Computed property for available next statuses
const availableNextStatuses = computed(() => {
  if (!order.value || !order.value.status) return [];
  return validTransitions[order.value.status] || [];
});

// Computed: Can the current status be updated?
const canUpdateStatus = computed(() => {
  return availableNextStatuses.value.length > 0;
});


const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = null;
  order.value = null;
  updateSuccess.value = false; // Reset success message on load
  updateError.value = null;
  selectedNewStatus.value = ''; // Reset selection
  try {
    const response = await getAdminOrderDetail(props.orderId);
    order.value = response.data;
  } catch (err) {
    console.error(`Error fetching order detail for ID ${props.orderId}:`, err);
    error.value = "Không thể tải chi tiết đơn hàng.";
    if (err.response?.status === 404) {
      error.value = "Đơn hàng không tồn tại.";
    } else if (err.response?.status === 403) {
      error.value = "Bạn không có quyền xem đơn hàng này.";
    }
  } finally {
    loading.value = false;
  }
};

const handleUpdateStatus = async () => {
  if (!selectedNewStatus.value || updatingStatus.value) return;

  updatingStatus.value = true;
  updateError.value = null;
  updateSuccess.value = false;

  try {
    const response = await updateOrderStatus(props.orderId, selectedNewStatus.value);
    // Update local order state with the response data
    order.value = response.data; // Assuming backend returns updated detail DTO
    updateSuccess.value = true;
    selectedNewStatus.value = ''; // Reset selection

    // Hide success message after a delay
    setTimeout(() => { updateSuccess.value = false; }, 3000);

  } catch (err) {
    console.error(`Error updating order status for ID ${props.orderId}:`, err);
    updateError.value = err.response?.data?.message || "Cập nhật trạng thái thất bại.";
    // Hide error message after a delay
    setTimeout(() => { updateError.value = null; }, 5000);
  } finally {
    updatingStatus.value = false;
  }
};

// --- Helpers ---
const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
const formatDateTime = (dateString) => dateString ? new Date(dateString).toLocaleString('vi-VN') : '';
const onImageError = (event) => { event.target.src = defaultImage; };
const formatStatus = (status) => allStatuses[status] || status || 'N/A';
const getStatusClass = (status) => { /* ... copy from AdminOrderListView ... */
  const classMap = {
    'PENDING': 'text-bg-secondary',
    'PROCESSING': 'text-bg-info',
    'SHIPPING': 'text-bg-primary',
    'COMPLETED': 'text-bg-success',
    'CANCELLED': 'text-bg-danger',
    'PAYMENT_PENDING': 'text-bg-warning text-dark', // Ensure text readable on yellow
  };
  return classMap[status] || 'text-bg-light';
};


// Fetch data when component mounts or orderId changes
watch(() => props.orderId, fetchOrderDetail, { immediate: true });

</script>

<style scoped>
.admin-order-detail-view dt {
  font-weight: 500;
  color: #6c757d; /* secondary color */
}
.admin-order-detail-view dd {
  margin-bottom: 0.5rem;
}
.badge.fs-6 { /* Make status badge slightly larger */
  padding: 0.4em 0.8em;
}
</style>
