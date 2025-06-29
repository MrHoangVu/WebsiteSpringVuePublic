<template>
  <div class="user-order-detail-view container mt-4 mb-5">
    <!-- Back Button -->
    <div class="mb-3">
      <router-link :to="{ name: 'orderHistory' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Lịch sử
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
      <div v-if="errorCode === 403" class="mt-2">Bạn không có quyền xem đơn hàng này.</div>
      <div v-if="errorCode === 404" class="mt-2">Đơn hàng không tồn tại.</div>
    </div>

    <!-- Order Detail Content -->
    <div v-else-if="order" class="order-detail-content">
      <h1 class="mb-4">
        Chi tiết Đơn hàng <strong class="text-primary">#{{ order.orderId }}</strong>
      </h1>

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

                <dt class="col-sm-4" v-if="order.orderNote">Ghi chú:</dt>
                <dd class="col-sm-8" v-if="order.orderNote" style="white-space: pre-wrap">
                  {{ order.orderNote }}
                </dd>
              </dl>
            </div>
          </div>

          <!-- Recipient Info Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Thông tin giao hàng</strong></div>
            <div class="card-body">
              <dl class="row mb-0">
                <dt class="col-sm-4">Người nhận:</dt>
                <dd class="col-sm-8">{{ order.shippingRecipientName }}</dd>

                <dt class="col-sm-4">Điện thoại:</dt>
                <dd class="col-sm-8">{{ order.shippingRecipientPhone }}</dd>

                <dt class="col-sm-4">Địa chỉ:</dt>
                <dd class="col-sm-8">
                  {{ order.shippingStreetAddress || ""
                  }}{{ order.shippingWard ? ", " + order.shippingWard : ""
                  }}{{ order.shippingDistrict ? ", " + order.shippingDistrict : ""
                  }}{{ order.shippingCity ? ", " + order.shippingCity : "" }}
                </dd>

                <dt class="col-sm-4">Vận chuyển:</dt>
                <dd class="col-sm-8">
                  {{ order.shippingMethodName || "N/A" }} ({{ formatCurrency(order.shippingCost) }})
                </dd>

                <dt class="col-sm-4" v-if="order.promotionCode">Khuyến mãi:</dt>
                <dd class="col-sm-8 text-success" v-if="order.promotionCode">
                  {{ order.promotionCode }} (-{{ formatCurrency(order.discountAmount) }})
                </dd>
              </dl>
            </div>
          </div>
        </div>

        <!-- Right Column: Items -->
        <div class="col-lg-5">
          <!-- Order Items Card -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><strong>Sản phẩm đã đặt</strong></div>
            <div class="card-body p-0">
              <ul class="list-group list-group-flush">
                <li
                  v-for="item in order.items"
                  :key="`${item.productId}-${item.priceAtPurchase}`"
                  class="list-group-item d-flex align-items-center"
                >
                  <img
                    :src="item.productImageUrl || defaultImage"
                    :alt="item.productName"
                    width="50"
                    height="50"
                    style="object-fit: cover"
                    class="me-3 rounded border"
                    @error="onImageError"
                  />
                  <div class="flex-grow-1">
                    <div class="fw-medium">{{ item.productName }}</div>
                    <small class="text-muted"
                      >SL: {{ item.quantity }} x {{ formatCurrency(item.priceAtPurchase) }}</small
                    >
                  </div>
                  <span class="fw-bold ms-2">{{
                    formatCurrency(item.quantity * (item.priceAtPurchase || 0))
                  }}</span>
                </li>
              </ul>
            </div>
            <!-- Order Summary Footer -->
            <div class="card-footer bg-light">
              <ul class="list-unstyled mb-0 small">
                <li class="d-flex justify-content-between">
                  <span>Tạm tính:</span>
                  <span>{{ formatCurrency(order.subtotal) }}</span>
                </li>
                <li class="d-flex justify-content-between">
                  <span>Phí vận chuyển:</span>
                  <span>{{ formatCurrency(order.shippingCost) }}</span>
                </li>
                <li
                  v-if="order.discountAmount > 0"
                  class="d-flex justify-content-between text-success"
                >
                  <span>Giảm giá ({{ order.promotionCode || "KM" }}):</span>
                  <span>- {{ formatCurrency(order.discountAmount) }}</span>
                </li>
                <li class="d-flex justify-content-between fw-bold fs-6 border-top pt-2 mt-2">
                  <span>Tổng cộng:</span>
                  <span class="text-danger">{{ formatCurrency(order.totalAmount) }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- Add action buttons for user if needed (e.g., Re-order, Request Return) -->
          <!--
          <div class="card shadow-sm">
              <div class="card-body text-center">
                  <button class="btn btn-outline-secondary me-2">Yêu cầu hỗ trợ</button>
                  <button class="btn btn-primary">Mua lại</button>
              </div>
          </div>
          -->
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
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { getUserOrderDetail } from "@/http/modules/public/orderService.js"; // Import service mới
import defaultImage from "@/assets/images/placeholder.png";
import {
  formatCurrency,
  formatDateTime,
  formatStatus,
  getStatusClass,
} from "@/utils/formatters.js"; // Import helpers

const props = defineProps({
  orderId: {
    type: [String, Number],
    required: true,
  },
});

const route = useRoute();
const router = useRouter();

const order = ref(null);
const loading = ref(true);
const error = ref(null);
const errorCode = ref(null); // Lưu mã lỗi HTTP

const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = null;
  errorCode.value = null;
  order.value = null;
  try {
    // Gọi API mới cho user detail
    const response = await getUserOrderDetail(props.orderId);
    order.value = response.data;
  } catch (err) {
    console.error(`Error fetching user order detail for ID ${props.orderId}:`, err);
    error.value = err.response?.data?.message || "Không thể tải chi tiết đơn hàng.";
    errorCode.value = err.response?.status || 500;
    if (errorCode.value === 403) {
      error.value = "Bạn không có quyền xem đơn hàng này.";
    } else if (errorCode.value === 404) {
      error.value = "Đơn hàng không tồn tại.";
    }
  } finally {
    loading.value = false;
  }
};

// Helper cho ảnh (giữ nguyên)
const onImageError = (event) => {
  event.target.src = defaultImage;
};

// Fetch data khi component mounts hoặc orderId prop thay đổi
watch(() => props.orderId, fetchOrderDetail, { immediate: true });
</script>

<style scoped>
.user-order-detail-view {
  min-height: 80vh;
}
.user-order-detail-view dt {
  font-weight: 500;
  color: #6c757d;
}
.user-order-detail-view dd {
  margin-bottom: 0.5rem;
}
.badge.fs-6 {
  padding: 0.4em 0.8em;
}
.list-group-item img {
  flex-shrink: 0;
}
</style>
