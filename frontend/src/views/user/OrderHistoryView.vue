<template>
  <div class="order-history-view container mt-4 mb-5">
    <h1 class="text-center mb-4">Lịch Sử Đơn Hàng</h1>

    <!-- Loading/Error/No Orders States (Giữ nguyên) -->
    <div v-if="loading" class="text-center my-5">...</div>
    <div v-else-if="error" class="alert alert-danger">...</div>
    <div v-else-if="orders.length === 0" class="alert alert-info text-center">...</div>

    <!-- Order List -->
    <div v-else>
      <div class="list-group shadow-sm">
        <div
          v-for="order in orders"
          :key="order.orderId"
          class="list-group-item list-group-item-action flex-column align-items-start mb-3 rounded border"
        >
          <!-- Thông tin đơn hàng (Giữ nguyên) -->
          <div class="d-flex w-100 justify-content-between mb-2 flex-wrap">
            <h5 class="mb-1 h6">
              Mã đơn hàng: <strong class="text-primary">#{{ order.orderId }}</strong>
            </h5>
            <small class="text-muted" :title="formatFullDateTime(order.orderDate)">{{
              formatDateRelative(order.orderDate)
            }}</small>
          </div>
          <div
            v-if="order.items && order.items.length > 0"
            class="mb-2 order-items-preview d-flex flex-wrap gap-2"
          >
            <img
              v-for="item in order.items.slice(0, 5)"
              :key="item.productId"
              :src="item.productImageUrl || defaultImage"
              :alt="item.productName"
              class="rounded border"
              width="50"
              height="50"
              style="object-fit: cover"
              :title="`${item.productName} (x${item.quantity})`"
              @error="onImageError"
            />
            <span v-if="order.items.length > 5" class="align-self-center text-muted small ms-1"
              >+ {{ order.items.length - 5 }} SP khác</span
            >
          </div>
          <div class="d-flex justify-content-between align-items-center flex-wrap">
            <p class="mb-1">
              Tổng tiền:
              <strong class="text-danger">{{ formatCurrency(order.totalAmount) }}</strong>
            </p>
            <span class="badge rounded-pill" :class="getStatusClass(order.status)">{{
              formatStatus(order.status)
            }}</span>
          </div>
          <small class="text-muted">Thanh toán: {{ order.paymentMethod }}</small>

          <!-- Action Buttons (Cập nhật nút Xem chi tiết) -->
          <div class="mt-2 text-end">
            <!-- === THAY THẾ BUTTON BẰNG ROUTER-LINK === -->
            <router-link
              :to="{ name: 'userOrderDetail', params: { orderId: order.orderId } }"
              class="btn btn-sm btn-outline-primary"
            >
              <i class="bi bi-eye"></i> Xem chi tiết
            </router-link>
            <!-- ======================================= -->
          </div>
        </div>
      </div>
      <!-- Pagination (Giữ nguyên) -->
      <BasePagination
        v-if="totalPages > 1"
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
        class="mt-4 d-flex justify-content-center"
      />
    </div>
  </div>
</template>

<script setup>
// --- Script setup giữ nguyên logic fetch và pagination ---
import { ref, onMounted, watch } from "vue";
import { useRouter, useRoute, RouterLink } from "vue-router"; // <<< Đảm bảo có RouterLink
import { getUserOrders } from "@/http/modules/public/orderService.js";
import BasePagination from "@/components/common/BasePagination.vue";
import defaultImage from "@/assets/images/placeholder.png";
// Giả định các hàm format được import hoặc định nghĩa
const formatCurrency = (value) =>
  value != null
    ? new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(value)
    : "0 đ";
const formatFullDateTime = (dateString) =>
  dateString ? new Date(dateString).toLocaleString("vi-VN") : "";
const formatDateRelative = (dateString) => {
  /* ... */
  return dateString ? new Date(dateString).toLocaleDateString("vi-VN") : "";
};
const formatStatus = (status) => {
  /* ... */
  return status || "N/A";
};
const getStatusClass = (status) => {
  /* ... */
  return "text-bg-light";
};

const router = useRouter();
const route = useRoute();
const orders = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const itemsPerPage = ref(10);

const fetchOrders = async (page = 0) => {
  /* ... logic fetch giữ nguyên ... */
  loading.value = true;
  error.value = null;
  try {
    const params = { page, size: itemsPerPage.value };
    const response = await getUserOrders(params);
    orders.value = response.data.content || [];
    currentPage.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch (err) {
    error.value = "Không thể tải danh sách đơn hàng.";
    orders.value = [];
    totalPages.value = 0;
  } finally {
    loading.value = false;
  }
};
const handlePageChange = (newPage) => {
  router.push({ query: { ...route.query, page: newPage } });
};
watch(
  () => route.query.page,
  (newPageQuery) => {
    fetchOrders(parseInt(newPageQuery, 10) || 0);
  },
  { immediate: true }
);
const onImageError = (event) => {
  event.target.src = defaultImage;
};

// Hàm viewOrderDetail không còn cần thiết vì đã dùng router-link
// const viewOrderDetail = (orderId) => { ... };
</script>

<style scoped>
/* Style không đổi */
.order-history-view {
  min-height: 70vh;
}

.list-group-item {
  transition: background-color 0.15s ease-in-out;
}

.order-items-preview img {
  opacity: 0.9;
}

.badge {
  font-size: 0.8em;
}
</style>
