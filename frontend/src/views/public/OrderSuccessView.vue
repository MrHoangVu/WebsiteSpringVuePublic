// src/views/OrderSuccessView.vue
<template>
  <div class="order-success-view container mt-5 mb-5 text-center">
    <div class="card shadow-sm p-4 p-md-5 mx-auto" style="max-width: 600px;">
      <div class="checkmark-container mb-3">
        <svg class="checkmark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 52 52">
          <circle class="checkmark__circle" cx="26" cy="26" r="25" fill="none"/>
          <path class="checkmark__check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
        </svg>
      </div>

      <h1 class="h3 mb-3 text-success">Đặt Hàng Thành Công!</h1>

      <p class="lead">Cảm ơn bạn đã mua hàng tại Tượng Gỗ Phong Thủy!</p>

      <p v-if="orderId" class="mb-4">
        Mã đơn hàng của bạn là: <strong class="text-primary">#{{ orderId }}</strong>
      </p>
      <p v-else class="text-muted mb-4">Đơn hàng của bạn đang được xử lý.</p>

      <!-- Optional: Display basic order details if fetched -->
      <!--
      <div v-if="loadingOrder" class="my-3"><div class="spinner-border spinner-border-sm"></div> Đang tải chi tiết...</div>
      <div v-if="orderError" class="alert alert-warning small">{{ orderError }}</div>
      <div v-if="orderDetails" class="order-details-summary text-start small border rounded p-3 mb-4">
          <strong>Ngày đặt:</strong> {{ formatDateTime(orderDetails.orderDate) }} <br>
          <strong>Tổng tiền:</strong> {{ formatCurrency(orderDetails.finalAmount) }} <br>
          <strong>Trạng thái:</strong> {{ orderDetails.orderStatus }} <br>
          <strong>PT Thanh toán:</strong> {{ orderDetails.paymentMethod }}
      </div>
      -->

      <p>Chúng tôi sẽ liên hệ với bạn sớm nhất để xác nhận đơn hàng (nếu cần thiết) và tiến hành giao hàng.</p>
      <p>Bạn có thể kiểm tra email để xem chi tiết đơn hàng (nếu có).</p>

      <div class="mt-4 d-flex justify-content-center gap-3">
        <router-link :to="{ name: 'home' }" class="btn btn-outline-secondary">
          <i class="bi bi-house-door"></i> Về Trang Chủ
        </router-link>
        <!-- Optional: Link to order history page -->
        <router-link v-if="!isGuest" :to="{ name: 'home' }" class="btn btn-primary"> {/* Thay 'home' bằng route lịch sử đơn hàng */}
          <i class="bi bi-receipt"></i> Xem Lịch Sử Đơn Hàng
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
import { useAuthStore } from '@/store/auth.js';
// Optional: Import service để lấy chi tiết đơn hàng
// import { getOrderSummaryById } from '@/http/orderService'; // Cần tạo service và API này

const route = useRoute();
const authStore = useAuthStore();

const orderId = ref(route.params.orderId || null);
const isGuest = computed(() => !authStore.isAuthenticated);

// Optional: State for fetching order details
// const orderDetails = ref(null);
// const loadingOrder = ref(false);
// const orderError = ref(null);

// Optional: Function to fetch order details
// const fetchOrderDetails = async () => {
//   if (!orderId.value) return;
//   loadingOrder.value = true;
//   orderError.value = null;
//   try {
//     // const response = await getOrderSummaryById(orderId.value); // Gọi API backend
//     // orderDetails.value = response.data;
//     await new Promise(res => setTimeout(res, 500)); // Giả lập fetch
//     orderDetails.value = { // Dữ liệu giả lập
//        orderDate: new Date().toISOString(),
//        finalAmount: 1234000,
//        orderStatus: 'PENDING',
//        paymentMethod: 'COD'
//     };
//   } catch (err) {
//     console.error("Error fetching order details:", err);
//     orderError.value = "Không thể tải chi tiết đơn hàng.";
//   } finally {
//     loadingOrder.value = false;
//   }
// };

// Optional: Helpers for formatting
// const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
// const formatDateTime = (dateString) => dateString ? new Date(dateString).toLocaleString('vi-VN') : '';

onMounted(() => {
  console.log("Order Success Page Mounted. Order ID:", orderId.value);
  // Optional: Fetch order details if needed
  // fetchOrderDetails();
});
</script>

<style scoped>
.order-success-view {
  min-height: 70vh;
  display: flex;
  align-items: center; /* Căn giữa card theo chiều dọc */
}

.checkmark-container {
  width: 100px;
  height: 100px;
  margin: 0 auto;
}

.checkmark__circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 3; /* Dày hơn một chút */
  stroke-miterlimit: 10;
  stroke: #4CAF50; /* Màu xanh lá */
  fill: none;
  animation: stroke 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: block;
  stroke-width: 3; /* Dày hơn */
  stroke: #fff;
  stroke-miterlimit: 10;
  margin: 10% auto;
  box-shadow: inset 0px 0px 0px #4CAF50;
  animation: fill .4s ease-in-out .4s forwards, scale .3s ease-in-out .9s both;
}

.checkmark__check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 4; /* Dày hơn */
  animation: stroke 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.8s forwards;
}

@keyframes stroke {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes scale {
  0%, 100% {
    transform: none;
  }
  50% {
    transform: scale3d(1.1, 1.1, 1);
  }
}

@keyframes fill {
  100% {
    box-shadow: inset 0px 0px 0px 50px #4CAF50; /* Kích thước fill */
  }
}

.order-details-summary {
  background-color: #f8f9fa;
}
</style>
