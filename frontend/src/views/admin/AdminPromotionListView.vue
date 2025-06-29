<template>
  <div class="admin-promotion-list-view">
    <!-- Tiêu đề và Nút Thêm mới -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1>Quản lý Khuyến mãi</h1>
      <router-link :to="{ name: 'adminPromotionNew' }" class="btn btn-primary">
        <i class="bi bi-plus-lg"></i> Thêm Khuyến mãi
      </router-link>
    </div>

    <!-- Bộ lọc -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <form @submit.prevent="applyTableFilters">
          <div class="row g-2 align-items-end">
            <!-- Bộ lọc Trạng thái -->
            <div class="col-md-4">
              <label for="filterStatus" class="form-label">Trạng thái</label>
              <select
                class="form-select form-select-sm"
                id="filterStatus"
                v-model="filters.statusFilter"
              >
                <option :value="null">-- Tất cả --</option>
                <option value="ACTIVE">Đang diễn ra</option>
                <option value="UPCOMING">Sắp diễn ra</option>
                <option value="EXPIRED">Đã hết hạn</option>
                <option value="INACTIVE">Ngừng hoạt động</option>
              </select>
            </div>
            <!-- TODO: Thêm các bộ lọc khác nếu cần (theo code, loại, bậc KH...) -->

            <!-- Nút Lọc và Xóa lọc -->
            <div class="col-md-auto">
              <button type="submit" class="btn btn-primary btn-sm w-100">
                <i class="bi bi-funnel"></i> Lọc
              </button>
            </div>
            <div class="col-md-auto">
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm w-100"
                @click="resetFilters"
              >
                <i class="bi bi-arrow-repeat"></i> Xóa lọc
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- Trạng thái Loading -->
    <div v-if="loading" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
    </div>

    <!-- Trạng thái Lỗi -->
    <div v-else-if="error" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ error }}
    </div>

    <!-- Bảng Dữ liệu -->
    <div v-else-if="promotions.length > 0" class="table-responsive card shadow-sm">
      <table class="table table-hover table-striped mb-0 align-middle">
        <thead class="table-light">
          <tr>
            <th scope="col">Mã Code</th>
            <th scope="col">Tên KM</th>
            <th scope="col">Loại</th>
            <th scope="col" class="text-end">Giá trị</th>
            <th scope="col">Ngày hiệu lực</th>
            <th scope="col">Ngày hết hạn</th>
            <th scope="col" class="text-center">Lượt dùng</th>
            <th scope="col">Bậc KH</th>
            <th scope="col" class="text-center">Trạng thái</th>
            <th scope="col" class="text-center" style="min-width: 100px">Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="promo in promotions" :key="promo.id">
            <!-- Mã Code -->
            <td class="fw-medium">{{ promo.code }}</td>
            <!-- Tên KM -->
            <td>{{ promo.name || "N/A" }}</td>
            <!-- Loại Giảm giá -->
            <td>{{ formatDiscountType(promo.discountType) }}</td>
            <!-- Giá trị Giảm giá -->
            <td class="text-end">
              {{ formatDiscountValue(promo.discountValue, promo.discountType) }}
            </td>
            <!-- Ngày Bắt đầu -->
            <td>{{ formatDate(promo.startDate) }}</td>
            <!-- Ngày Kết thúc -->
            <td>{{ formatDate(promo.endDate) }}</td>
            <!-- Lượt dùng -->
            <td class="text-center">{{ promo.currentUsage ?? 0 }} / {{ promo.maxUsage || "∞" }}</td>
            <!-- Bậc Khách hàng -->
            <td>
              <span v-if="!promo.targetTiers" class="badge bg-light text-dark border">Tất cả</span>
              <span v-else>
                <span
                  v-for="tier in promo.targetTiers
                    .split(',')
                    .map((t) => t.trim())
                    .filter((t) => t)"
                  :key="tier"
                  class="badge me-1"
                  :class="getTierClass(tier)"
                >
                  {{ formatTier(tier) }}
                </span>
              </span>
            </td>
            <!-- Trạng thái -->
            <td class="text-center">
              <span class="badge rounded-pill" :class="getPromoStatusClass(promo)">
                {{ getPromoStatusText(promo) }}
              </span>
            </td>
            <!-- Hành động -->
            <td class="text-center">
              <!-- Nút Sửa -->
              <router-link
                :to="{ name: 'adminPromotionEdit', params: { id: promo.id } }"
                class="btn btn-sm btn-outline-secondary me-1"
                title="Sửa"
              >
                <i class="bi bi-pencil-square"></i>
              </router-link>
              <!-- Nút Ngừng/Kích hoạt -->
              <button
                class="btn btn-sm"
                :class="promo.isActive ? 'btn-outline-danger' : 'btn-outline-success'"
                :title="promo.isActive ? 'Ngừng hoạt động' : 'Kích hoạt lại'"
                @click="confirmToggleStatus(promo)"
                :disabled="togglingStatusId === promo.id"
              >
                <!-- Spinner khi đang xử lý -->
                <span
                  v-if="togglingStatusId === promo.id"
                  class="spinner-border spinner-border-sm"
                  role="status"
                  aria-hidden="true"
                ></span>
                <!-- Icon tương ứng -->
                <i
                  v-else
                  :class="promo.isActive ? 'bi bi-pause-circle' : 'bi bi-play-circle-fill'"
                ></i>
              </button>
              <!-- TODO: Thêm nút xóa nếu cần -->
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Phân trang -->
      <div class="card-footer bg-light border-top-0 py-2" v-if="totalPages > 1">
        <BasePagination
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
          class="mt-0 d-flex justify-content-center mb-0"
        />
      </div>
    </div>

    <!-- Trạng thái Không có dữ liệu -->
    <div v-else class="alert alert-info text-center">
      <i class="bi bi-info-circle me-2"></i> Không tìm thấy chương trình khuyến mãi nào phù hợp với
      bộ lọc hiện tại.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { useRouter, useRoute, RouterLink } from "vue-router";
import {
  getAdminPromotions,
  toggleAdminPromotionStatus,
} from "@/http/modules/admin/adminPromotionService.js";
import BasePagination from "@/components/common/BasePagination.vue";
// Đảm bảo import đủ các hàm formatters
import { formatCurrency, formatDate, formatTier, getTierClass } from "@/utils/formatters";

const router = useRouter();
const route = useRoute();

// State cho danh sách, loading, lỗi, phân trang
const promotions = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const itemsPerPage = ref(10); // Số lượng item mỗi trang

// State cho việc toggle trạng thái (để hiển thị spinner)
const togglingStatusId = ref(null);

// State cho bộ lọc (dùng reactive để tiện theo dõi)
const filters = reactive({
  statusFilter: route.query.statusFilter || null, // Lấy giá trị ban đầu từ URL query
});

// --- Hàm Fetch Dữ liệu ---
const fetchPromotions = async (page = 0) => {
  loading.value = true;
  error.value = null;
  promotions.value = []; // Xóa dữ liệu cũ trước khi fetch
  try {
    // Chuẩn bị tham số cho API call
    const params = {
      page,
      size: itemsPerPage.value,
      sort: "createdAt,desc", // Sắp xếp mặc định theo ngày tạo mới nhất
      // Thêm tham số lọc vào API call nếu có giá trị
      ...(filters.statusFilter && { statusFilter: filters.statusFilter }),
      // ...(filters.otherFilter && { otherFilter: filters.otherFilter }) // Ví dụ thêm filter khác
    };
    console.log("Fetching promotions with params:", params); // Debug log
    const response = await getAdminPromotions(params);
    promotions.value = response.data.content || [];
    currentPage.value = response.data.number; // Trang hiện tại (bắt đầu từ 0)
    totalPages.value = response.data.totalPages; // Tổng số trang
  } catch (err) {
    console.error("Error fetching admin promotions:", err);
    error.value = `Không thể tải danh sách khuyến mãi. Lỗi: ${
      err.response?.data?.message || err.message || "Unknown error"
    }`;
  } finally {
    loading.value = false;
  }
};

// --- Hàm Xử lý Bộ lọc ---
const applyTableFilters = () => {
  // Khi áp dụng bộ lọc, luôn quay về trang đầu tiên (trang 0)
  // Cập nhật URL query để phản ánh trạng thái filter
  const query = { ...route.query, page: 0 }; // Reset về trang 0
  if (filters.statusFilter) {
    query.statusFilter = filters.statusFilter;
  } else {
    delete query.statusFilter; // Xóa khỏi query nếu giá trị là null/rỗng
  }
  // Các filter khác tương tự
  router.push({ query });
  // Watcher trên route.query sẽ tự động gọi lại fetchPromotions
};

const resetFilters = () => {
  // Reset state của bộ lọc
  filters.statusFilter = null;
  // Reset URL query (chỉ giữ lại page nếu có, hoặc về trang 0)
  const query = { ...route.query };
  delete query.statusFilter;
  // delete query.otherFilter; // Xóa các filter khác nếu có
  query.page = 0; // Luôn về trang 0 khi reset
  router.push({ query });
  // Watcher sẽ gọi lại fetchPromotions
};

// --- Hàm Toggle Trạng thái (isActive) ---
// <<< THAY THẾ HÀM NÀY >>>
const confirmToggleStatus = async (promo) => {
  // Kiểm tra nếu đang có thao tác khác diễn ra
  if (togglingStatusId.value) return;

  // Xác định trạng thái mới và hành động tương ứng
  const newStatus = !promo.isActive;
  const actionText = newStatus ? "kích hoạt lại" : "ngừng hoạt động";

  // Hỏi xác nhận người dùng
  if (!confirm(`Bạn có chắc muốn ${actionText} khuyến mãi "${promo.code}"?`)) {
    return; // Người dùng hủy
  }

  // Đặt trạng thái loading cho dòng này
  togglingStatusId.value = promo.id;

  try {
    // Gọi hàm API mới từ service đã import
    const response = await toggleAdminPromotionStatus(promo.id, newStatus);

    // Cập nhật dữ liệu trong bảng từ response trả về (cách này tốt hơn là chỉ đổi isActive)
    const updatedPromo = response.data;
    const promoIndex = promotions.value.findIndex((p) => p.id === updatedPromo.id);
    if (promoIndex !== -1) {
      // Thay thế toàn bộ object cũ bằng object mới từ API
      promotions.value[promoIndex] = updatedPromo;
      console.log(`Promotion ${promo.id} status toggled successfully on UI.`);
    } else {
      // Nếu không tìm thấy (hiếm khi xảy ra), fetch lại toàn bộ trang
      console.warn(`Could not find promotion ${promo.id} in local list after toggle. Refetching.`);
      await fetchPromotions(currentPage.value);
    }

    // (Tùy chọn) Hiển thị thông báo thành công (ví dụ: dùng toast)
  } catch (err) {
    // Xử lý lỗi
    console.error(`Error toggling status for promotion ${promo.id}:`, err);
    // Hiển thị thông báo lỗi cụ thể hơn cho người dùng
    const errorMsg =
      err.response?.data?.message || err.message || `Lỗi khi ${actionText} khuyến mãi.`;
    alert(errorMsg); // Hoặc dùng toast
  } finally {
    togglingStatusId.value = null;
  }
};

// --- Hàm Helpers Định dạng ---
const formatDiscountType = (type) => {
  if (type === "PERCENTAGE") return "Phần trăm (%)";
  if (type === "FIXED_AMOUNT") return "Số tiền cố định (VND)";
  return type; // Trả về nguyên gốc nếu không khớp
};

const formatDiscountValue = (value, type) => {
  if (value === null || value === undefined) return "N/A";
  if (type === "PERCENTAGE") return `${value}%`;
  // Giả định type còn lại là FIXED_AMOUNT
  return formatCurrency(value); // Sử dụng hàm formatCurrency đã import
};

// Xác định text trạng thái dựa trên ngày và isActive
const getPromoStatusText = (promo) => {
  const now = new Date();
  const start = new Date(promo.startDate);
  const end = new Date(promo.endDate);

  if (!promo.isActive) return "Ngừng";
  // Chuyển đổi Date về dạng YYYY-MM-DD để so sánh chỉ ngày, tránh ảnh hưởng giờ phút giây
  const nowDateOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const startDateOnly = new Date(start.getFullYear(), start.getMonth(), start.getDate());
  const endDateOnly = new Date(end.getFullYear(), end.getMonth(), end.getDate());

  if (nowDateOnly < startDateOnly) return "Sắp diễn ra";
  // Đã qua ngày kết thúc (end date là ngày cuối cùng được áp dụng)
  if (nowDateOnly > endDateOnly) return "Đã hết hạn";
  // Nếu không rơi vào các trường hợp trên, tức là đang trong khoảng thời gian hiệu lực
  return "Đang diễn ra";
};

// Xác định class CSS cho badge trạng thái
const getPromoStatusClass = (promo) => {
  const statusText = getPromoStatusText(promo); // Gọi hàm lấy text để tái sử dụng logic
  switch (statusText) {
    case "Ngừng":
      return "text-bg-secondary"; // Màu xám cho Ngừng
    case "Sắp diễn ra":
      return "text-bg-info"; // Màu xanh dương nhạt cho Sắp diễn ra
    case "Đã hết hạn":
      return "text-bg-warning text-dark"; // Màu vàng cho Đã hết hạn
    case "Đang diễn ra":
      return "text-bg-success"; // Màu xanh lá cho Đang diễn ra
    default:
      return "text-bg-light text-dark border"; // Mặc định
  }
};
// Lưu ý: Các hàm formatTier và getTierClass cần được copy từ AdminUserListView.vue
// hoặc import từ utils/formatters nếu đã tách ra.

// --- Watchers & Lifecycle ---

// Theo dõi sự thay đổi của query parameters trên URL (page, filters)
watch(
  () => route.query,
  (newQuery, oldQuery) => {
    console.log("Route query changed:", newQuery); // Debug log
    // Cập nhật state 'filters' từ URL để giữ đồng bộ khi back/forward trình duyệt
    filters.statusFilter = newQuery.statusFilter || null;
    // Cập nhật các filter khác nếu có...

    // Fetch lại dữ liệu với trang và filter mới từ URL
    // Chuyển đổi page query sang số nguyên, mặc định là 0 nếu không có
    const page = parseInt(newQuery.page || "0", 10);
    fetchPromotions(page);
  },
  {
    immediate: true, // Chạy ngay lần đầu khi component được tạo
    deep: true, // Theo dõi sâu các thay đổi trong object query
  }
);

// Xử lý sự kiện thay đổi trang từ component Pagination
const handlePageChange = (newPage) => {
  // Cập nhật URL query với trang mới, giữ nguyên các filter hiện tại
  router.push({ query: { ...route.query, page: newPage } });
  // Watcher sẽ tự động gọi fetchPromotions
};

// // onMounted được thay thế bằng watch với immediate: true
// onMounted(() => {
//   // Fetch dữ liệu lần đầu dựa trên query params hiện tại
//   fetchPromotions(parseInt(route.query.page || '0', 10));
// });
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle; /* Căn giữa nội dung trong ô theo chiều dọc */
}
.badge {
  font-size: 0.8em; /* Kích thước badge nhỏ hơn một chút */
  padding: 0.3em 0.5em;
}
.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03); /* Hiệu ứng hover nhẹ */
}
/* Responsive: có thể ẩn bớt cột trên màn hình nhỏ nếu cần */
/* @media (max-width: 768px) {
  .table th:nth-child(3), .table td:nth-child(3), // Loại
  .table th:nth-child(8), .table td:nth-child(8)  // Bậc KH
   { display: none; }
} */
</style>
