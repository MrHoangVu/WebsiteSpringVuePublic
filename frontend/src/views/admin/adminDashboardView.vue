<template>
  <div class="admin-dashboard container mt-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1>Thống kê doanh thu</h1>
    </div>

    <!-- Hàng chứa các Summary Cards -->
    <div class="row mb-4">
      <!-- Thống kê hôm nay -->
      <div class="col-md-6 col-lg-4 mb-3">
        <div class="card text-white bg-primary h-100">
          <div class="card-header">Hôm Nay ({{ todayDate }})</div>
          <div class="card-body">
            <div
              v-if="loadingStats.today"
              class="spinner-border spinner-border-sm text-white"
              role="status"
            >
              <span class="visually-hidden">Loading...</span>
            </div>
            <div v-else>
              <h5 class="card-title display-6">
                {{ stats.today ? stats.today.totalOrders : "--" }}
              </h5>
              <p class="card-text">Đơn hàng</p>
              <hr />
              <h5 class="card-title display-6">
                {{ stats.today ? formatCurrency(stats.today.totalRevenue) : "--" }}
              </h5>
              <p class="card-text">Doanh thu</p>
            </div>
            <div v-if="errorStats.today" class="text-white-50 small mt-2">Lỗi tải dữ liệu</div>
          </div>
        </div>
      </div>
      <div class="col-md-6 col-lg-4 mb-3">
        <div class="card text-white bg-success h-100">
          <div class="card-header">7 Ngày Gần Nhất</div>
          <div class="card-body">
            <div
              v-if="loadingStats.last7days"
              class="spinner-border spinner-border-sm text-white"
              role="status"
            >
              <span class="visually-hidden">Loading...</span>
            </div>
            <div v-else>
              <h5 class="card-title display-6">
                {{ stats.last7days ? stats.last7days.totalOrders : "--" }}
              </h5>
              <p class="card-text">Đơn hàng</p>
              <hr />
              <h5 class="card-title display-6">
                {{ stats.last7days ? formatCurrency(stats.last7days.totalRevenue) : "--" }}
              </h5>
              <p class="card-text">Doanh thu</p>
            </div>
            <div v-if="errorStats.last7days" class="text-white-50 small mt-2">Lỗi tải dữ liệu</div>
          </div>
        </div>
      </div>
      <div class="col-md-6 col-lg-4 mb-3">
        <div class="card text-dark bg-warning h-100">
          <div class="card-header">Tháng {{ currentMonth }}</div>
          <div class="card-body">
            <div
              v-if="loadingStats.currentMonth"
              class="spinner-border spinner-border-sm text-dark"
              role="status"
            >
              <span class="visually-hidden">Loading...</span>
            </div>
            <div v-else>
              <h5 class="card-title display-6">
                {{ stats.currentMonth ? stats.currentMonth.totalOrders : "--" }}
              </h5>
              <p class="card-text">Đơn hàng</p>
              <hr />
              <h5 class="card-title display-6">
                {{ stats.currentMonth ? formatCurrency(stats.currentMonth.totalRevenue) : "--" }}
              </h5>
              <p class="card-text">Doanh thu</p>
            </div>
            <div v-if="errorStats.currentMonth" class="text-danger small mt-2">Lỗi tải dữ liệu</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hàng chứa Biểu đồ Doanh thu -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <div v-if="loadingChart" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading Chart...</span>
              </div>
            </div>
            <div v-else-if="errorChart" class="alert alert-danger">
              Lỗi tải biểu đồ: {{ errorChart.message }}
            </div>
            <!-- Thẻ canvas để vẽ biểu đồ -->
            <Line v-else :data="chartData" :options="chartOptions" style="height: 350px" />
          </div>
        </div>
      </div>
    </div>

    <!-- Hàng chứa Bộ lọc Ngày Tùy chỉnh và Kết quả -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Xem Thống Kê Theo Khoảng Ngày</h5>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end mb-3">
              <div class="col-md-5">
                <label for="startDate" class="form-label">Từ ngày</label>
                <input type="date" class="form-control" id="startDate" v-model="customDate.start" />
              </div>
              <div class="col-md-5">
                <label for="endDate" class="form-label">Đến ngày</label>
                <input type="date" class="form-control" id="endDate" v-model="customDate.end" />
              </div>
              <div class="col-md-2">
                <button
                  class="btn btn-secondary w-100"
                  @click="fetchCustomStats"
                  :disabled="loadingCustomStats || !customDate.start || !customDate.end"
                >
                  <span
                    v-if="loadingCustomStats"
                    class="spinner-border spinner-border-sm me-1"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  Xem
                </button>
              </div>
            </div>
            <div v-if="errorCustomStats" class="alert alert-danger mt-2">
              {{ errorCustomStats.message }}
            </div>
            <!-- Hiển thị kết quả tùy chỉnh -->
            <div v-if="loadingCustomStats && !statsCustom" class="text-center text-muted">
              Đang tải...
            </div>
            <div v-if="statsCustom" class="mt-3 p-3 bg-light rounded row">
              <div class="col-md-6">
                <h4>
                  Tổng đơn: <span class="text-primary">{{ statsCustom.totalOrders }}</span>
                </h4>
              </div>
              <div class="col-md-6">
                <h4>
                  Tổng doanh thu:
                  <span class="text-success">{{ formatCurrency(statsCustom.totalRevenue) }}</span>
                </h4>
              </div>
            </div>
            <div
              v-if="
                !loadingCustomStats &&
                !statsCustom &&
                customDate.start &&
                customDate.end &&
                !errorCustomStats
              "
              class="mt-3 text-muted text-center"
            >
              Không có dữ liệu cho khoảng ngày đã chọn.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from "@/store/auth";
import { useRouter } from "vue-router";
import { onMounted, ref, reactive, computed, watch } from "vue";
// --- CHART.JS IMPORTS ---
import { Line } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler, // <<< 1. IMPORT THÊM Filler TỪ 'chart.js'
} from "chart.js";
// -------------------------
// Import stats service
import {
  getTodayStats,
  getLast7DaysStats,
  getCurrentMonthStats,
  getBasicStats,
  getRevenueOverTime,
} from "@/http/modules/public/statsService.js";

// --- ĐĂNG KÝ COMPONENTS CHART.JS ---
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler // <<< 1. IMPORT THÊM Filler TỪ 'chart.js'
);
// ---------------------------------

const authStore = useAuthStore();
const router = useRouter();

// State cho summary cards
const loadingStats = reactive({ today: false, last7days: false, currentMonth: false });
const stats = reactive({ today: null, last7days: null, currentMonth: null });
const errorStats = reactive({ today: null, last7days: null, currentMonth: null });

// --- STATE CHO CHART ---
const loadingChart = ref(false);
const errorChart = ref(null);
const chartData = ref({
  labels: [],
  datasets: [
    {
      label: "Doanh thu (VND)",
      backgroundColor: "rgba(0, 123, 255, 0.2)",
      borderColor: "rgba(0, 123, 255, 1)",
      data: [],
      fill: true,
      tension: 0.1,
    },
  ],
});
const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
    },
    title: {
      display: true,
      text: "Biểu đồ Doanh thu Theo Ngày",
    },
    tooltip: {
      callbacks: {
        label: function (context) {
          let label = context.dataset.label || "";
          if (label) {
            label += ": ";
          }
          if (context.parsed.y !== null && context.parsed.y !== undefined) {
            label += new Intl.NumberFormat("vi-VN", {
              style: "currency",
              currency: "VND",
            }).format(context.parsed.y);
          } else {
            label += "0 VND";
          }
          return label;
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function (value, index, values) {
          if (value >= 1000000) return value / 1000000 + " Tr";
          if (value >= 1000) return value / 1000 + " K";
          return value;
        },
      },
    },
    x: {
      title: {
        display: true,
        text: "Ngày",
      },
    },
  },
});
// -------------------------

// --- STATE CHO CUSTOM RANGE ---
const defaultEndDate = new Date().toISOString().split("T")[0]; // Hôm nay
const defaultStartDate = new Date(new Date().setDate(new Date().getDate() - 6))
  .toISOString()
  .split("T")[0]; // 7 ngày trước

const customDate = ref({ start: defaultStartDate, end: defaultEndDate });
const loadingCustomStats = ref(false);
const errorCustomStats = ref(null);
const statsCustom = ref(null); // Lưu kết quả thống kê tùy chỉnh
// ------------------------------

const todayDate = computed(() => new Date().toLocaleDateString("vi-VN"));
const currentMonth = computed(() => new Date().getMonth() + 1);

// Hàm đăng xuất
const handleLogout = () => {
  authStore.logout();
  router.push("/login");
};

// Hàm định dạng tiền tệ
const formatCurrency = (value) => {
  if (value === null || value === undefined) return "--";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
};

// Hàm fetch dữ liệu summary cards
const fetchSummaryStats = async (type) => {
  loadingStats[type] = true;
  errorStats[type] = null;
  try {
    let response;
    if (type === "today") response = await getTodayStats();
    else if (type === "last7days") response = await getLast7DaysStats();
    else if (type === "currentMonth") response = await getCurrentMonthStats();
    else return;

    if (response && response.data) {
      stats[type] = response.data;
    } else {
      throw new Error("Dữ liệu không hợp lệ");
    }
  } catch (err) {
    console.error(`Error fetching ${type} stats:`, err);
    errorStats[type] = err;
    stats[type] = null;
  } finally {
    loadingStats[type] = false;
  }
};

// --- HÀM FETCH CHART DATA ---
const fetchChartData = async (startDate, endDate) => {
  loadingChart.value = true;
  errorChart.value = null;
  // Cập nhật tiêu đề biểu đồ theo khoảng ngày
  chartOptions.value.plugins.title.text = `Doanh thu từ ${formatDate(startDate)} đến ${formatDate(
    endDate
  )}`;
  try {
    const response = await getRevenueOverTime(startDate, endDate);
    const apiData = response.data || []; // Đảm bảo luôn có một mảng

    if (!Array.isArray(apiData)) {
      throw new Error("Dữ liệu không đúng định dạng");
    }

    // Map dữ liệu API sang format của Chart.js
    chartData.value = {
      labels: apiData.map((point) => formatDate(point?.date || "", "dd/MM")),
      datasets: [
        {
          ...chartData.value.datasets[0],
          data: apiData.map((point) => point?.revenue || 0),
        },
      ],
    };
  } catch (err) {
    console.error("Error fetching chart data:", err);
    errorChart.value = err;
    // Reset chart data nếu lỗi
    chartData.value = {
      labels: [],
      datasets: [{ ...chartData.value.datasets[0], data: [] }],
    };
  } finally {
    loadingChart.value = false;
  }
};
// -------------------------------

// --- HÀM FETCH CUSTOM STATS ---
const fetchCustomStats = async () => {
  if (!customDate.value.start || !customDate.value.end) {
    errorCustomStats.value = new Error("Vui lòng chọn ngày bắt đầu và kết thúc.");
    return;
  }
  if (new Date(customDate.value.start) > new Date(customDate.value.end)) {
    errorCustomStats.value = new Error("Ngày bắt đầu không được sau ngày kết thúc.");
    return;
  }

  loadingCustomStats.value = true;
  errorCustomStats.value = null;
  statsCustom.value = null; // Reset trước khi fetch

  try {
    const response = await getBasicStats(customDate.value.start, customDate.value.end);
    if (response && response.data) {
      statsCustom.value = response.data;
      // Đồng thời cập nhật luôn biểu đồ theo khoảng ngày mới chọn
      await fetchChartData(customDate.value.start, customDate.value.end);
    } else {
      throw new Error("Không nhận được dữ liệu hợp lệ");
    }
  } catch (err) {
    console.error("Error fetching custom stats:", err);
    errorCustomStats.value = err;
    statsCustom.value = null; // Reset khi có lỗi
  } finally {
    loadingCustomStats.value = false;
  }
};
// ------------------------------

// Helper format date
const formatDate = (dateString, format = "dd/MM/yyyy") => {
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString; // Trả về chuỗi gốc nếu ngày không hợp lệ

    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();

    if (format === "dd/MM") return `${day}/${month}`;
    return `${day}/${month}/${year}`; // Default dd/MM/yyyy
  } catch (e) {
    console.error("Error formatting date:", e);
    return dateString; // Trả về chuỗi gốc nếu không parse được
  }
};

// Theo dõi thay đổi khoảng ngày tùy chỉnh
watch([() => customDate.value.start, () => customDate.value.end], (newValues, oldValues) => {
  // Reset thông báo lỗi khi thay đổi ngày
  if (newValues !== oldValues) {
    errorCustomStats.value = null;
  }
});

onMounted(() => {
  try {
    fetchSummaryStats("today");
    fetchSummaryStats("last7days");
    fetchSummaryStats("currentMonth");
    fetchCustomStats();
  } catch (error) {
    console.error("Error during initial data fetching:", error);
  }
});
</script>

<style scoped>
.admin-dashboard {
  /* Style */
}

.card-header {
  font-weight: 500;
}

.card-body hr {
  opacity: 0.5;
}

.card-title {
  margin-bottom: 0.25rem;
}

.card-text {
  font-size: 0.9em;
  opacity: 0.9;
}

.display-6 {
  font-weight: 300; /* Làm số mảnh hơn chút */
}
</style>
