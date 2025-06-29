<template>
  <div class="admin-order-list-view container mt-4 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1>Quản lý Đơn hàng</h1>
      <!-- Optional: Add export/filter buttons -->
    </div>

    <!-- Filters & Search -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <form @submit.prevent="applyTableFilters">
          <div class="row g-3 align-items-end">
            <!-- Keyword Search -->
            <div class="col-md-4 col-lg-3">
              <label for="filterKeyword" class="form-label">Tìm kiếm</label>
              <input type="text" class="form-control form-control-sm" id="filterKeyword" v-model="filters.keyword" placeholder="Mã ĐH, tên, SĐT, email...">
            </div>
            <!-- Status Filter -->
            <div class="col-md-3 col-lg-2">
              <label for="filterStatus" class="form-label">Trạng thái</label>
              <select class="form-select form-select-sm" id="filterStatus" v-model="filters.status">
                <option :value="null">-- Tất cả --</option>
                <option v-for="statusOpt in statusOptions" :key="statusOpt.value" :value="statusOpt.value">
                  {{ statusOpt.text }}
                </option>
              </select>
            </div>
            <!-- Date Range Filter -->
            <div class="col-md-5 col-lg-4">
              <label class="form-label">Ngày đặt hàng</label>
              <div class="input-group input-group-sm">
                <input type="date" class="form-control" title="Từ ngày" v-model="filters.startDate">
                <span class="input-group-text">đến</span>
                <input type="date" class="form-control" title="Đến ngày" v-model="filters.endDate">
              </div>
            </div>
            <!-- Action Buttons -->
            <div class="col-md-12 col-lg-3 d-flex gap-2 mt-3 mt-lg-0">
              <button type="submit" class="btn btn-primary btn-sm flex-grow-1">
                <i class="bi bi-search"></i> Tìm kiếm / Lọc
              </button>
              <button type="button" class="btn btn-outline-secondary btn-sm flex-grow-1" @click="resetFilters" title="Xóa bộ lọc">
                <i class="bi bi-x-lg"></i> Xóa lọc
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-5 py-5">
      <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
        <span class="visually-hidden">Đang tải đơn hàng...</span>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger">
      Lỗi tải đơn hàng: {{ error }}
    </div>

    <!-- Order Table -->
    <div v-else-if="orders.length > 0" class="table-responsive card shadow-sm">
      <table class="table table-hover table-striped mb-0 align-middle">
        <thead class="table-light">
        <tr>
          <th scope="col">Mã ĐH</th>
          <th scope="col">Ngày Đặt</th>
          <th scope="col">Khách Hàng</th>
          <th scope="col" class="text-end">Tổng Tiền</th>
          <th scope="col" class="text-center">Trạng Thái</th>
          <th scope="col">Thanh Toán</th>
          <th scope="col" class="text-center">Hành Động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="order in orders" :key="order.orderId">
          <td class="fw-medium">#{{ order.orderId }}</td>
          <td>{{ formatDateTime(order.orderDate) }}</td>
          <td>{{ order.customerInfo }}</td>
          <td class="text-end">{{ formatCurrency(order.totalAmount) }}</td>
          <td class="text-center">
              <span class="badge rounded-pill" :class="getStatusClass(order.status)">
                {{ formatStatus(order.status) }}
              </span>
          </td>
          <td>{{ order.paymentMethod }}</td>
          <td class="text-center">
            <router-link
              :to="{ name: 'adminOrderDetail', params: { orderId: order.orderId } }"
              class="btn btn-sm btn-outline-primary"
              title="Xem chi tiết"
            >
              <i class="bi bi-eye"></i>
            </router-link>
          </td>
        </tr>
        </tbody>
      </table>
      <!-- Card Footer for Pagination -->
      <div class="card-footer bg-light border-top-0" v-if="totalPages > 1">
        <BasePagination
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
          class="mb-0 mt-3 d-flex justify-content-center"
        />
      </div>
    </div>

    <!-- No Orders State -->
    <div v-else class="alert alert-info text-center">
      Không có đơn hàng nào khớp với tiêu chí.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'; // <<< THÊM reactive, computed
import { useRouter, useRoute, RouterLink } from 'vue-router';
import { getAllOrders } from '@/http/modules/admin/adminOrderService.js'; // <<< KIỂM TRA ĐƯỜNG DẪN
import BasePagination from '@/components/common/BasePagination.vue';
// <<< IMPORT ĐẦY ĐỦ FORMATTERS >>>
import { allStatuses, formatStatus, formatCurrency, formatDateTime, getStatusClass } from '@/utils/formatters';

const router = useRouter();
const route = useRoute();

const orders = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const itemsPerPage = ref(15);

// State cho bộ lọc/tìm kiếm
const filters = reactive({ // <<< DÙNG REACTIVE
  keyword: route.query.keyword || '',
  status: route.query.status || null,
  startDate: route.query.startDate || null,
  endDate: route.query.endDate || null,
});

// Danh sách trạng thái cho dropdown
const statusOptions = computed(() => { // <<< DÙNG COMPUTED
  return allStatuses.map(s => ({ value: s, text: formatStatus(s) }));
});

// Hàm áp dụng bộ lọc
const applyTableFilters = () => {
  const query = { ...route.query, page: 0 };
  if (filters.keyword) query.keyword = filters.keyword; else delete query.keyword;
  if (filters.status) query.status = filters.status; else delete query.status;
  if (filters.startDate) query.startDate = filters.startDate; else delete query.startDate;
  if (filters.endDate) query.endDate = filters.endDate; else delete query.endDate;
  if (filters.startDate && filters.endDate && filters.startDate > filters.endDate) {
    alert("Ngày bắt đầu không được sau ngày kết thúc.");
    return;
  }
  router.push({ query });
};

// Hàm xóa bộ lọc
const resetFilters = () => {
  filters.keyword = '';
  filters.status = null;
  filters.startDate = null;
  filters.endDate = null;
  const query = { ...route.query };
  delete query.keyword;
  delete query.status;
  delete query.startDate;
  delete query.endDate;
  query.page = 0;
  router.push({ query });
};

// Hàm fetch dữ liệu
const fetchOrders = async (page = 0) => {
  loading.value = true;
  error.value = null;
  try {
    const params = {
      page,
      size: itemsPerPage.value,
      sort: route.query.sort || 'orderDate,desc',
      // Gửi filter state hiện tại
      ...(filters.keyword && { keyword: filters.keyword }),
      ...(filters.status && { status: filters.status }),
      ...(filters.startDate && { startDate: filters.startDate }),
      ...(filters.endDate && { endDate: filters.endDate }),
    };
    console.log("Fetching orders with params:", params); // Log params để debug
    const response = await getAllOrders(params);
    orders.value = response.data.content || [];
    currentPage.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch (err) {
    console.error("Error fetching admin orders:", err);
    error.value = "Không thể tải danh sách đơn hàng.";
    orders.value = []; totalPages.value = 0;
  } finally {
    loading.value = false;
  }
};

// Hàm xử lý chuyển trang
const handlePageChange = (newPage) => {
  router.push({ query: { ...route.query, page: newPage } });
};

// Watch sự thay đổi của route query
watch(
  () => route.query,
  (newQuery) => {
    console.log('Route query changed:', newQuery); // Log để debug

    // Cập nhật state filter từ URL
    filters.keyword = newQuery.keyword || '';
    filters.status = newQuery.status || null;
    filters.startDate = newQuery.startDate || null;
    filters.endDate = newQuery.endDate || null;

    // Cập nhật trang hiện tại
    const pageNum = parseInt(newQuery.page || '0', 10);

    // Fetch lại dữ liệu với trang và filter mới
    // Chỉ fetch nếu trang hoặc filter thực sự thay đổi để tránh gọi thừa
    // (Kiểm tra này có thể phức tạp, tạm thời gọi luôn)
    fetchOrders(pageNum);
  },
  { immediate: true, deep: true } // Chạy ngay và theo dõi sâu
);

</script>

<style scoped>
.admin-order-list-view { min-height: 80vh; }
.table th, .table td { vertical-align: middle; }
.badge { font-size: 0.8em; padding: 0.4em 0.7em; }
/* Thêm style cho input date nhỏ hơn nếu cần */
.input-group-sm .form-control[type="date"] {
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
  font-size: 0.875rem;
}
</style>
