<template>
  <div class="product-list-view container mt-4 mb-5">
    <!-- Header: Title and Actions -->
    <div class="row mb-4 align-items-center">
      <!-- Page Title -->
      <div class="col-md-6 col-lg-8">
        <h1 class="h3 mb-0">{{ pageTitle }}</h1>
        <!-- Product Count -->
        <small v-if="!loading && totalElements !== null" class="text-muted d-block mt-1">
          Tìm thấy {{ totalElements }} sản phẩm
        </small>
      </div>

      <!-- Filter & Sort Buttons -->
      <div class="col-md-6 col-lg-4 text-md-end mt-3 mt-md-0">
        <!-- Filter Button (Mobile) -->
        <button
          class="btn btn-outline-secondary me-2 d-lg-none"
          type="button"
          data-bs-toggle="offcanvas"
          data-bs-target="#filterOffcanvas"
          aria-controls="filterOffcanvas"
        >
          <i class="bi bi-funnel"></i> Bộ lọc
        </button>
        <!-- Sort Dropdown -->
        <!-- Sort Dropdown -->
        <div class="dropdown d-inline-block position-relative">
          <!-- Thêm position-relative nếu menu định vị absolute -->
          <button
            ref="sortButton"
            class="btn btn-outline-secondary dropdown-toggle"
            type="button"
            id="sortDropdown"
            aria-expanded="false"
            @click="toggleSortDropdown"
          >
            <i class="bi bi-sort-down me-1"></i> {{ selectedSortLabel }}
          </button>
          <ul
            ref="sortMenu"
            class="dropdown-menu dropdown-menu-end"
            :class="{ show: isSortDropdownOpen }"
            aria-labelledby="sortDropdown"
            style="
              /* Có thể cần style định vị nếu không dùng Popper */
              /* Ví dụ: position: absolute; top: 100%; right: 0; */
            "
          >
            <li v-for="option in sortOptions" :key="option.value">
              <a
                class="dropdown-item"
                :class="{ active: currentSort === option.value }"
                href="#"
                @click.prevent="handleSortSelection(option.value)"
              >
                {{ option.label }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div
      v-if="loading"
      key="loading-state"
      class="d-flex justify-content-center align-items-center my-5 py-5"
      style="min-height: 40vh"
    >
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem" role="status">
        <span class="visually-hidden">Đang tải sản phẩm...</span>
      </div>
      <span class="ms-3 fs-5 text-muted">Đang tải sản phẩm...</span>
    </div>

    <!-- Error State -->
    <div
      v-else-if="error"
      key="error-state"
      class="alert alert-danger text-center py-4"
      role="alert"
    >
      <i class="bi bi-exclamation-triangle-fill me-2 fs-4 align-middle"></i>
      <span>Lỗi tải dữ liệu: {{ error.message || "Không thể tải danh sách sản phẩm." }}</span>
      <p class="mt-2 small">
        Vui lòng thử <a href="#" @click.prevent="refreshData">tải lại trang</a> hoặc quay lại sau.
      </p>
    </div>

    <!-- Main Content Area -->
    <div v-else key="content-area" class="row g-4">
      <!-- Filter Panel (Desktop) -->
      <div class="col-lg-3 d-none d-lg-block">
        <div class="filter-panel card shadow-sm sticky-top" style="top: 80px">
          <div class="card-header bg-light">
            <h5 class="mb-0"><i class="bi bi-funnel-fill me-1"></i> Bộ lọc</h5>
          </div>
          <div class="card-body filter-content-desktop">
            <!-- Sử dụng FilterContent, truyền filters hiện tại -->
            <FilterContent
              :initial-filters="currentFilters"
              @apply="applyFilters"
              @reset="resetFilters"
            />
          </div>
        </div>
      </div>

      <!-- Product Grid & Pagination -->
      <div class="col-12 col-lg-9">
        <!-- Applied Filters Display -->
        <div
          v-if="hasActiveFilters"
          class="alert alert-light small p-2 d-flex flex-wrap gap-2 align-items-center mb-3 border"
        >
          <strong class="me-1">Đang lọc theo:</strong>
          <!-- Hiển thị các filter đang áp dụng (ví dụ) -->
          <span
            v-if="
              currentFilters.categories &&
              currentFilters.categories.length > 0 &&
              !route.params.categorySlug
            "
            class="badge text-bg-light border"
          >
            Danh mục <span class="text-muted">({{ currentFilters.categories.length }})</span>
          </span>
          <span
            v-if="currentFilters.minPrice !== null || currentFilters.maxPrice !== null"
            class="badge text-bg-light border"
          >
            Giá: {{ formatCurrency(currentFilters.minPrice ?? 0) }} -
            {{ currentFilters.maxPrice ? formatCurrency(currentFilters.maxPrice) : "..." }}
          </span>
          <span v-if="currentFilters.material" class="badge text-bg-light border">
            Chất liệu: {{ currentFilters.material }}
          </span>
          <span v-if="currentFilters.size" class="badge text-bg-light border">
            Kích thước: {{ formatSize(currentFilters.size) }}
          </span>
          <button
            class="btn btn-link btn-sm text-danger p-0 ms-auto fw-normal"
            @click="resetFilters"
          >
            (Xóa tất cả)
          </button>
        </div>

        <!-- Product List -->
        <div
          v-if="products.length > 0"
          class="row row-cols-1 row-cols-sm-2 row-cols-md-2 row-cols-xl-3 g-4 mb-4"
        >
          <ProductCard v-for="product in products" :key="product.id" :product="product" />
        </div>
        <!-- No Products Found Message -->
        <div v-else class="alert alert-info text-center py-5">
          <i class="bi bi-search fs-1 d-block mb-3 text-secondary"></i>
          <h4>Không tìm thấy sản phẩm nào</h4>
          <p class="text-muted">Vui lòng thử thay đổi bộ lọc hoặc tìm kiếm từ khóa khác.</p>
          <button class="btn btn-outline-secondary mt-2" @click="resetFilters">Xóa bộ lọc</button>
        </div>

        <!-- Pagination -->
        <BasePagination
          v-if="totalPages > 1"
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
          class="d-flex justify-content-center mt-4 pt-2 border-top"
        />
      </div>
    </div>

    <!-- Filter Offcanvas (Mobile) -->
    <div
      class="offcanvas offcanvas-start d-lg-none"
      tabindex="-1"
      id="filterOffcanvas"
      aria-labelledby="filterOffcanvasLabel"
    >
      <div class="offcanvas-header border-bottom">
        <h5 class="offcanvas-title" id="filterOffcanvasLabel">
          <i class="bi bi-funnel"></i> Bộ lọc sản phẩm
        </h5>
        <button
          type="button"
          class="btn-close"
          data-bs-dismiss="offcanvas"
          aria-label="Close"
        ></button>
      </div>
      <div class="offcanvas-body filter-content-mobile">
        <!-- Sử dụng FilterContent, truyền filters hiện tại -->
        <FilterContent
          :initial-filters="currentFilters"
          @apply="applyFiltersAndClose"
          @reset="resetFiltersAndClose"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getProducts } from "@/http/modules/public/productService.js";
import { getCategoryBySlug } from "@/http/modules/public/categoryService.js";
import ProductCard from "@/components/product/ProductCard.vue";
import BasePagination from "@/components/common/BasePagination.vue";
import FilterContent from "@/components/product/FilterContent.vue"; // Import FilterContent
import { formatCurrency } from "@/utils/formatters.js"; // Import formatter nếu cần
import { Offcanvas } from "bootstrap"; // Import Offcanvas để đóng bằng JS

const route = useRoute();
const router = useRouter();

// --- State ---
const products = ref([]);
const currentCategory = ref(null); // Thông tin category hiện tại (nếu ở trang category)
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const totalElements = ref(null);
const itemsPerPage = ref(12); // Số sản phẩm/trang

// Sorting state
const sortOptions = ref([
  { value: "createdAt,desc", label: "Mới nhất" },
  { value: "price,asc", label: "Giá: Thấp đến cao" },
  { value: "price,desc", label: "Giá: Cao đến thấp" },
  { value: "name,asc", label: "Tên: A-Z" },
]);
console.log("Sort Options on Setup:", JSON.stringify(sortOptions.value)); // <<< THÊM DÒNG NÀY

const currentSort = ref(route.query.sort || sortOptions.value[0].value); // Lấy từ URL hoặc mặc định

// --- Filters State ---
// Computed property lấy filter hiện tại từ URL query (Source of Truth)
const currentFilters = computed(() => {
  const query = route.query;
  return {
    // Chuyển categories từ string query (vd:"1,2,3") thành mảng string ID
    categories: query.categories ? String(query.categories).split(",") : [],
    minPrice: query.minPrice !== undefined && query.minPrice !== "" ? Number(query.minPrice) : null,
    maxPrice: query.maxPrice !== undefined && query.maxPrice !== "" ? Number(query.maxPrice) : null,
    material: query.material || null,
    size: query.size || null, // Thêm size filter
    // Thêm các filter khác nếu có
  };
});

// Computed kiểm tra xem có filter nào đang active không
const hasActiveFilters = computed(() => {
  const filters = currentFilters.value;
  // Không tính category filter nếu đang ở trang category cụ thể
  const categoryFilterActive = !route.params.categorySlug && filters.categories.length > 0;
  return (
    categoryFilterActive ||
    filters.minPrice !== null ||
    filters.maxPrice !== null ||
    filters.material !== null ||
    filters.size !== null
  );
});

// --- Computed Properties for Display ---
const pageTitle = computed(() => {
  if (route.query.keyword) {
    return `Kết quả tìm kiếm: "${route.query.keyword}"`;
  } else if (currentCategory.value) {
    return `Sản phẩm: ${currentCategory.value.name}`;
  } else {
    return "Tất cả sản phẩm";
  }
});

const selectedSortLabel = computed(() => {
  const selected = sortOptions.value.find((opt) => opt.value === currentSort.value);
  return selected ? selected.label : "Sắp xếp";
});

// Helper format size (ví dụ)
const formatSize = (sizeValue) => {
  const map = { small: "Nhỏ", medium: "Vừa", large: "Lớn" };
  return map[sizeValue] || sizeValue;
};

// --- Fetch Data ---
const fetchData = async () => {
  loading.value = true;
  error.value = null;
  currentCategory.value = null;
  totalElements.value = null;
  let categoryIdParam = null; // ID category lấy từ slug (nếu có)

  const categorySlug = route.params.categorySlug;

  // 1. Fetch category info nếu có slug
  if (categorySlug) {
    try {
      const categoryResponse = await getCategoryBySlug(categorySlug);
      currentCategory.value = categoryResponse.data;
      categoryIdParam = currentCategory.value.id; // Lấy ID để gửi API
    } catch (catErr) {
      console.error(`Error fetching category by slug "${categorySlug}":`, catErr);
      // Có thể hiển thị lỗi hoặc fallback về hiển thị tất cả
      error.value = new Error(
        `Không tìm thấy danh mục '${categorySlug}'. Đang hiển thị tất cả sản phẩm.`
      );
      // Không gán categoryIdParam, API sẽ trả về tất cả
    }
  }

  // 2. Lấy page và sort từ URL query hiện tại
  const queryPage = parseInt(route.query.page || "0", 10);
  const querySort = route.query.sort || sortOptions.value[0].value;

  // Cập nhật state nội bộ cho sort/page (dù computed đã có nhưng để code rõ ràng)
  currentPage.value = queryPage;
  currentSort.value = querySort;

  // 3. Tạo params cho API call
  const params = {
    page: currentPage.value,
    size: itemsPerPage.value,
    sort: currentSort.value,
    ...(route.query.keyword && { keyword: route.query.keyword }), // Keyword search
    // Chỉ gửi categoryId từ slug NẾU CÓ và KHÔNG gửi categoryIds từ filter
    ...(categoryIdParam && { categoryId: categoryIdParam }),
    // Chỉ gửi categoryIds từ filter NẾU KHÔNG có categoryId từ slug
    ...(!categoryIdParam &&
      currentFilters.value.categories.length > 0 && {
        categoryIds: currentFilters.value.categories.join(","),
      }),
    // Gửi các filter khác
    ...(currentFilters.value.minPrice !== null && { minPrice: currentFilters.value.minPrice }),
    ...(currentFilters.value.maxPrice !== null && { maxPrice: currentFilters.value.maxPrice }),
    ...(currentFilters.value.material && { material: currentFilters.value.material }),
    ...(currentFilters.value.size && { size: currentFilters.value.size }),
    // Thêm các filter khác nếu có
  };

  // Loại bỏ các param rỗng/null/undefined trước khi gửi (optional but good practice)
  Object.keys(params).forEach(
    (key) =>
      (params[key] === null || params[key] === undefined || params[key] === "") &&
      delete params[key]
  );

  console.log("Fetching products with params:", params);

  // 4. Gọi API
  try {
    const response = await getProducts(params);
    // 5. Xử lý response
    if (
      response.data &&
      Array.isArray(response.data.content) &&
      response.data.totalPages !== undefined &&
      response.data.number !== undefined
    ) {
      products.value = response.data.content;
      totalPages.value = response.data.totalPages;
      currentPage.value = response.data.number; // API trả về trang hiện tại (0-based)
      totalElements.value = response.data.totalElements;
    } else {
      console.error("Invalid API response structure:", response.data);
      throw new Error("Dữ liệu trả về không đúng định dạng.");
    }
  } catch (prodErr) {
    // 6. Xử lý lỗi
    console.error("Error fetching products:", prodErr);
    error.value = prodErr; // Lưu lỗi để hiển thị
    products.value = [];
    totalPages.value = 0;
    totalElements.value = 0; // Reset các giá trị khi lỗi
  } finally {
    // 7. Kết thúc loading
    loading.value = false;
  }
};

// --- Event Handlers ---
const handlePageChange = (newPage) => {
  // Chỉ cần cập nhật query, watcher sẽ fetch lại
  router.push({ query: { ...route.query, page: newPage } });
};

const setSort = (sortValue) => {
  // Cập nhật query, reset page về 0, watcher sẽ fetch lại
  router.push({ query: { ...route.query, sort: sortValue, page: 0 } });
};

// Xử lý sự kiện 'apply' từ FilterContent
const applyFilters = (newFilters) => {
  console.log("ProductListView: Applying filters", newFilters);
  const currentQuery = { ...route.query }; // Lấy query hiện tại
  const queryToUpdate = {}; // Query mới sẽ được build

  // --- Xử lý từng filter từ newFilters ---
  // Lưu ý: newFilters có thể chứa categories dạng mảng string hoặc dạng string đã join tùy vào FilterContent emit ra
  // Giả định FilterContent emit categories dạng string "1,2,3"

  if (newFilters.categories) {
    // Nếu có categories được emit
    queryToUpdate.categories = newFilters.categories; // Gán trực tiếp string "1,2,3"
  } else {
    delete currentQuery.categories; // Xóa khỏi query nếu filter categories bị bỏ chọn
  }

  if (newFilters.minPrice !== null && newFilters.minPrice !== undefined) {
    queryToUpdate.minPrice = newFilters.minPrice;
  } else {
    delete currentQuery.minPrice;
  }

  if (newFilters.maxPrice !== null && newFilters.maxPrice !== undefined) {
    queryToUpdate.maxPrice = newFilters.maxPrice;
  } else {
    delete currentQuery.maxPrice;
  }

  if (newFilters.material) {
    queryToUpdate.material = newFilters.material;
  } else {
    delete currentQuery.material;
  }

  if (newFilters.size) {
    queryToUpdate.size = newFilters.size;
  } else {
    delete currentQuery.size;
  }
  // Thêm xử lý cho các filter khác nếu có

  // --- Tạo query cuối cùng và điều hướng ---
  const finalQuery = {
    ...currentQuery, // Giữ lại các query cũ không liên quan (sort, keyword)
    ...queryToUpdate, // Ghi đè/thêm các filter mới/đã xóa
    page: 0, // Luôn reset về trang đầu khi áp dụng filter mới
  };

  // Dọn dẹp các key không còn giá trị (để URL sạch hơn)
  Object.keys(finalQuery).forEach((key) => {
    if (finalQuery[key] === null || finalQuery[key] === undefined || finalQuery[key] === "") {
      delete finalQuery[key];
    }
  });

  // Cập nhật URL, watcher sẽ tự động gọi fetchData
  router.push({ query: finalQuery });
};

// Xử lý sự kiện 'reset' từ FilterContent
const resetFilters = () => {
  console.log("ProductListView: Resetting filters");
  const query = { ...route.query };
  // Xóa tất cả các key liên quan đến filter
  delete query.categories;
  delete query.minPrice;
  delete query.maxPrice;
  delete query.material;
  delete query.size;
  // Thêm các key filter khác nếu có
  query.page = 0; // Reset về trang đầu

  // Cập nhật URL, watcher sẽ gọi fetchData
  router.push({ query });
};

// --- Handlers cho Offcanvas Mobile ---
const applyFiltersAndClose = (newFilters) => {
  applyFilters(newFilters);
  closeOffcanvas();
};

const resetFiltersAndClose = () => {
  resetFilters();
  closeOffcanvas();
};

const closeOffcanvas = () => {
  const offcanvasElement = document.getElementById("filterOffcanvas");
  if (offcanvasElement) {
    const bsOffcanvas = Offcanvas.getInstance(offcanvasElement);
    if (bsOffcanvas) {
      bsOffcanvas.hide();
    } else {
      // Fallback nếu chưa khởi tạo (hiếm gặp)
      const newBsOffcanvas = new Offcanvas(offcanvasElement);
      newBsOffcanvas.hide();
    }
  }
};

// --- Refresh Data ---
const refreshData = () => {
  error.value = null; // Reset lỗi trước khi thử lại
  fetchData();
};

// --- Watcher ---
// Theo dõi cả params (categorySlug) và query (page, sort, filters)
// Khi một trong hai thay đổi, gọi fetchData
watch(
  () => [route.params, route.query],
  () => {
    fetchData();
  },
  {
    immediate: true, // Gọi ngay lần đầu khi component mount
    deep: true, // Theo dõi sâu các thay đổi trong query object
  }
);

// --- State cho dropdown tự quản lý ---
const isSortDropdownOpen = ref(false);
const sortButton = ref(null); // Ref cho nút button
const sortMenu = ref(null); // Ref cho ul menu

// --- Hàm bật/tắt dropdown ---
const toggleSortDropdown = () => {
  isSortDropdownOpen.value = !isSortDropdownOpen.value;
  // Cập nhật aria-expanded cho trợ năng
  if (sortButton.value) {
    sortButton.value.setAttribute("aria-expanded", isSortDropdownOpen.value.toString());
  }
  console.log("Dropdown toggled:", isSortDropdownOpen.value); // Debug
};

// --- Hàm xử lý khi chọn một mục sắp xếp ---
const handleSortSelection = (sortValue) => {
  setSort(sortValue); // Gọi hàm setSort cũ của bạn
  isSortDropdownOpen.value = false; // Đóng dropdown sau khi chọn
  if (sortButton.value) {
    // Cập nhật lại aria
    sortButton.value.setAttribute("aria-expanded", "false");
  }
};

// --- Hàm xử lý đóng dropdown khi click ra ngoài ---
const handleClickOutsideSortDropdown = (event) => {
  // Kiểm tra xem dropdown có đang mở không
  if (!isSortDropdownOpen.value) return;

  // Kiểm tra xem click có xảy ra bên ngoài button VÀ bên ngoài menu không
  if (
    sortButton.value &&
    !sortButton.value.contains(event.target) &&
    sortMenu.value &&
    !sortMenu.value.contains(event.target)
  ) {
    isSortDropdownOpen.value = false;
    if (sortButton.value) {
      // Cập nhật lại aria
      sortButton.value.setAttribute("aria-expanded", "false");
    }
    console.log("Clicked outside, dropdown closed."); // Debug
  }
};

// --- Đăng ký và hủy đăng ký listener khi click ra ngoài ---
onMounted(() => {
  // Lắng nghe sự kiện click trên toàn bộ document
  document.addEventListener("click", handleClickOutsideSortDropdown);
});

// onMounted(() => {
//   // Không cần gọi fetchData ở đây vì watcher `immediate: true` đã làm việc đó
// });
</script>

<style scoped>
/* Style giữ nguyên hoặc tùy chỉnh thêm */


.filter-panel .card-body {
  max-height: calc(100vh - 120px); /* Điều chỉnh chiều cao tối đa */
  overflow-y: auto;
}

.dropdown-menu {
  /* Các style cơ bản từ Bootstrap đã có (position, min-width, ...) */
  /* Thêm transition cho các thuộc tính sẽ thay đổi */
  transition: opacity 0.15s ease-in-out, transform 0.15s ease-in-out, visibility 0.15s;

  /* Trạng thái ẩn ban đầu */
  opacity: 0;
  transform: translateY(-10px); /* Hơi đẩy lên trên một chút */
  visibility: hidden; /* Ẩn hoàn toàn và không thể tương tác */
  display: block; /* Luôn là block để transition hoạt động, visibility sẽ ẩn/hiện */
  /* Lưu ý: Nếu cách này gây layout shift, quay lại dùng display: none/block */
  /* và transition chỉ trên opacity/transform */
}

.dropdown-menu.show {
  /* Trạng thái hiển thị */
  opacity: 1;
  transform: translateY(0); /* Đưa về vị trí ban đầu */
  visibility: visible; /* Hiển thị và có thể tương tác */

  /* Các style cần ghi đè !important đã có từ trước (nếu cần) */
  /* display: block !important; */ /* Không cần nếu dùng visibility */
}

/* (Tùy chọn) Thêm hiệu ứng hover mượt hơn cho các item */
.dropdown-item {
  transition: background-color 0.1s ease-in-out, color 0.1s ease-in-out;
}

/* (Tùy chọn) Đảm bảo bóng đổ (shadow) - Bootstrap thường có sẵn) */
.dropdown-menu {
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15); /* Giá trị shadow của Bootstrap 5 */
  /* Kiểm tra bằng DevTools xem đã có chưa, nếu chưa thì thêm vào */
}

/* (Tùy chọn) Đảm bảo z-index đủ cao */
.dropdown-menu {
  z-index: 1021; /* Giá trị z-index phổ biến cho dropdown, kiểm tra xem BS đã đặt chưa */
}

/* Đảm bảo thẻ cha có position: relative (đã làm trong HTML) */
.dropdown.position-relative {
  position: relative !important; /* Đảm bảo nó được áp dụng */
}
.dropdown-item.active,
.dropdown-item:active {
  color: #fff;
  background-color: var(--bs-primary);
  font-weight: 500;
}

.filter-content-mobile .btn {
  margin-top: 0.5rem;
}

/* Style cho khu vực hiển thị filter đang áp dụng */
.alert-light .badge {
  border-color: #ccc !important; /* Màu border nhẹ */
  background-color: #f8f9fa !important; /* Màu nền nhạt */
  color: var(--bs-body-color); /* Màu chữ mặc định */
  font-weight: 400; /* Font weight bình thường */
}

.alert-light .btn-link {
  text-decoration: none;
}
</style>
