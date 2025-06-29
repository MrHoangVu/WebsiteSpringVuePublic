<template>
  <!-- Loại bỏ navbar-light và bg-white để cho phép biến CSS kiểm soát màu sắc -->
  <nav class="navbar navbar-expand-lg shadow-sm sticky-top app-header">
    <div class="container">
      <button
        class="btn fixed-sidebar-toggle d-none d-lg-flex align-items-center"
        type="button"
        data-bs-toggle="offcanvas"
        data-bs-target="#appSidebar"
        aria-controls="appSidebar"
        aria-label="Mở menu điều hướng"
      >
        <i class="bi bi-list me-2"></i>
      </button>

      <router-link class="navbar-brand fw-bold app-logo" :to="{ name: 'home' }">
        <img
          src="https://doanhnhantredaklak.org/userfiles/users/107/1609494305008.png"
          alt="Logo"
          class="logo-img me-2"
        />
        Tượng Gỗ Phong Thủy
      </router-link>

      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#mainNavbarContent"
        aria-controls="mainNavbarContent"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="mainNavbarContent">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link class="nav-link px-lg-3" :to="{ name: 'home' }" active-class="active">
              Trang chủ
            </router-link>
          </li>

          <li
            class="nav-item dropdown"
            @mouseenter="openProductDropdown"
            @mouseleave="startCloseProductDropdownTimer"
          >
            <a
              ref="productDropdownTrigger"
              class="nav-link dropdown-toggle px-lg-3"
              href="#"
              role="button"
              :class="{ active: isProductRouteActive || isProductDropdownOpen }"
              :aria-expanded="isProductDropdownOpen"
              @click.prevent="handleDropdownTriggerClick($event, 'product')"
              aria-haspopup="true"
            >
              Sản phẩm
            </a>
            <ul
              ref="productDropdownMenu"
              class="dropdown-menu shadow-sm fade"
              :class="{ show: isProductDropdownOpen }"
              @mouseenter="cancelCloseProductDropdownTimer"
              @mouseleave="startCloseProductDropdownTimer"
              @click="closeProductDropdownOnClick"
            >
              <li>
                <router-link
                  class="dropdown-item"
                  :to="{ name: 'productList' }"
                  active-class="active"
                >
                  Tất cả sản phẩm
                </router-link>
              </li>
              <li v-if="!loadingCategories && categories.length > 0">
                <hr class="dropdown-divider" />
              </li>
              <li v-if="loadingCategories" class="dropdown-item disabled text-center small py-2">
                <div class="spinner-border spinner-border-sm text-secondary" role="status">
                  <span class="visually-hidden">Đang tải...</span>
                </div>
              </li>
              <li v-else-if="categoryError" class="dropdown-item disabled">
                <span class="text-danger small">{{ categoryError }}</span>
              </li>
              <li v-else v-for="category in categories" :key="category.id">
                <router-link
                  class="dropdown-item"
                  :to="{ name: 'productListByCategory', params: { categorySlug: category.slug } }"
                  active-class="active"
                >
                  {{ category.name }}
                </router-link>
              </li>
              <li
                v-if="!loadingCategories && !categoryError && categories.length === 0"
                class="dropdown-item disabled"
              >
                <!-- text-muted sẽ tự đổi màu theo theme nếu Bootstrap vars được định nghĩa đúng -->
                <span class="text-muted small">Không có danh mục.</span>
              </li>
            </ul>
          </li>

          <li class="nav-item">
            <router-link
              class="nav-link px-lg-3"
              :to="{ name: 'articleList' }"
              active-class="active"
            >
              Tin Tức
            </router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link px-lg-3" :to="{ name: 'contact' }" active-class="active">
              Liên hệ
            </router-link>
          </li>
        </ul>

        <div class="d-none d-lg-flex mx-lg-3 header-search-desktop">
          <SearchBar />
        </div>

        <ul class="navbar-nav ms-auto mb-2 mb-lg-0 align-items-lg-center header-actions">
          <template v-if="!authStore.isAuthenticated">
            <li class="nav-item">
              <router-link class="nav-link" :to="{ name: 'login' }" active-class="active">
                <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
              </router-link>
            </li>
            <li class="nav-item ms-lg-2">
              <router-link class="btn btn-outline-primary btn-sm" :to="{ name: 'register' }">
                <i class="bi bi-person-plus-fill me-1"></i> Đăng ký
              </router-link>
            </li>
          </template>
          <template v-else>
            <li
              class="nav-item dropdown"
              @mouseenter="openUserDropdown"
              @mouseleave="startCloseUserDropdownTimer"
            >
              <a
                ref="userDropdownTrigger"
                class="nav-link dropdown-toggle d-flex align-items-center"
                href="#"
                role="button"
                :class="{ active: isUserDropdownOpen }"
                :aria-expanded="isUserDropdownOpen"
                @click.prevent="handleDropdownTriggerClick($event, 'user')"
                aria-haspopup="true"
              >
                <i class="bi bi-person-circle fs-5 me-1"></i>
                <span class="d-none d-lg-inline"
                >Chào, {{ authStore.user?.username || "bạn" }}</span
                >
                <span class="d-inline d-lg-none">Tài khoản</span>
              </a>
              <ul
                ref="userDropdownMenu"
                class="dropdown-menu dropdown-menu-end shadow-sm fade"
                :class="{ show: isUserDropdownOpen }"
                @mouseenter="cancelCloseUserDropdownTimer"
                @mouseleave="startCloseUserDropdownTimer"
                @click="closeUserDropdownOnClick"
              >
                <template v-if="authStore.isAdmin">
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminDashboard' }">
                      <i class="bi bi-speedometer2"></i> Thống kê
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminOrderList' }">
                      <i class="bi bi-box-seam"></i> Quản lý Đơn hàng
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminProductList' }">
                      <i class="bi bi-tags"></i> Quản lý Sản phẩm
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminUserList' }">
                      <i class="bi bi-people nav-icon"></i> Quản lý khách hàng
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminShippingMethodList' }">
                      <i class="bi bi-truck nav-icon"></i> Quản lý vận chuyển
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminPromotionList' }">
                      <i class="bi bi-gift nav-icon"></i> Quản lý khuyến mãi
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'adminArticleList' }">
                      <i class="bi bi-journal-text nav-icon"></i> Quản lý bài viết
                    </router-link>
                  </li>
                  <li><hr class="dropdown-divider" /></li>
                </template>

                <template v-if="!authStore.isAdmin">
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'userProfile' }">
                      <i class="bi bi-person-lines-fill"></i> Tài khoản của tôi
                    </router-link>
                  </li>
                  <li>
                    <router-link class="dropdown-item" :to="{ name: 'orderHistory' }">
                      <i class="bi bi-receipt"></i> Đơn hàng của tôi
                    </router-link>
                  </li>
                </template>

                <li><hr class="dropdown-divider" /></li>
                <li>
                  <!-- text-danger sẽ tự đổi màu theo theme nếu Bootstrap vars được định nghĩa đúng -->
                  <button
                    class="dropdown-item text-danger d-flex align-items-center"
                    type="button"
                    @click="handleLogout"
                  >
                    <i class="bi bi-box-arrow-right"></i> Đăng xuất
                  </button>
                </li>
              </ul>
            </li>
          </template>

          <li class="nav-item ms-lg-3" v-if="!authStore.isAdmin">
            <router-link
              class="nav-link position-relative cart-icon-link"
              :to="{ name: 'shoppingCart' }"
              title="Giỏ hàng"
            >
              <i class="bi bi-cart-fill fs-5"></i>
              <span
                v-if="cartStore.totalItemsCount > 0"
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill cart-badge"
              >
                {{ cartStore.totalItemsCount > 99 ? "99+" : cartStore.totalItemsCount }}
                <span class="visually-hidden">sản phẩm</span>
              </span>
            </router-link>
          </li>
        </ul>

        <div class="d-lg-none my-2 header-search-mobile">
          <SearchBar />
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
// Script setup remains unchanged
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import { RouterLink, useRouter, useRoute } from "vue-router";
import SearchBar from "./SearchBar.vue";
import { useAuthStore } from "@/store/auth";
import { useCartStore } from "@/store/cart";
import { getAllCategories } from "@/http/modules/public/categoryService.js";

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();
const route = useRoute();

const categories = ref([]);
const loadingCategories = ref(false);
const categoryError = ref(null);

const isProductDropdownOpen = ref(false);
const isUserDropdownOpen = ref(false);
const productCloseTimer = ref(null);
const userCloseTimer = ref(null);
const closeDelay = 200;

const productDropdownTrigger = ref(null);
const productDropdownMenu = ref(null);
const userDropdownTrigger = ref(null);
const userDropdownMenu = ref(null);

const openProductDropdown = () => {
  cancelCloseProductDropdownTimer();
  if (isUserDropdownOpen.value) {
    closeUserDropdown();
  }
  isProductDropdownOpen.value = true;
};

const closeProductDropdown = () => {
  isProductDropdownOpen.value = false;
};

const startCloseProductDropdownTimer = () => {
  cancelCloseProductDropdownTimer();
  productCloseTimer.value = setTimeout(closeProductDropdown, closeDelay);
};

const cancelCloseProductDropdownTimer = () => {
  if (productCloseTimer.value) {
    clearTimeout(productCloseTimer.value);
    productCloseTimer.value = null;
  }
};

const openUserDropdown = () => {
  cancelCloseUserDropdownTimer();
  if (isProductDropdownOpen.value) {
    closeProductDropdown();
  }
  isUserDropdownOpen.value = true;
};

const closeUserDropdown = () => {
  isUserDropdownOpen.value = false;
};

const startCloseUserDropdownTimer = () => {
  cancelCloseUserDropdownTimer();
  userCloseTimer.value = setTimeout(closeUserDropdown, closeDelay);
};

const cancelCloseUserDropdownTimer = () => {
  if (userCloseTimer.value) {
    clearTimeout(userCloseTimer.value);
    userCloseTimer.value = null;
  }
};

const closeProductDropdownOnClick = (event) => {
  if (event.target.closest(".dropdown-item")) {
    closeProductDropdown();
  }
};
const closeUserDropdownOnClick = (event) => {
  if (event.target.closest(".dropdown-item")) {
    closeUserDropdown();
  }
};

const handleDropdownTriggerClick = (event, type) => {
  event.preventDefault();
};

const isProductRouteActive = computed(() => {
  return route.name === "productList" || route.name === "productListByCategory";
});

const fetchCategories = async () => {
  if (loadingCategories.value) return;
  loadingCategories.value = true;
  categoryError.value = null;
  try {
    const response = await getAllCategories();
    if (Array.isArray(response?.data)) {
      categories.value = response.data;
    } else {
      console.warn("API response for categories is not an array:", response?.data);
      categories.value = [];
      categoryError.value = "Dữ liệu danh mục không hợp lệ.";
    }
  } catch (err) {
    console.error("Error fetching categories in Header:", err);
    categoryError.value = "Lỗi tải danh mục. Vui lòng thử lại.";
    categories.value = [];
  } finally {
    loadingCategories.value = false;
  }
};

const handleLogout = () => {
  closeUserDropdown();
  authStore.logout();
  router.push({ name: "home" }).catch((err) => {
    if (
      err.name !== "NavigationDuplicated" &&
      !err.message.includes("Avoided redundant navigation")
    ) {
      console.error("Logout navigation error:", err);
    }
  });
};

onMounted(() => {
  fetchCategories();
  if (cartStore.items.length === 0 && !cartStore.isLoading && !cartStore.error) {
    cartStore.fetchCart().catch((err) => {
      console.error("AppHeader: Initial cart fetch failed:", err);
    });
  }
});

onBeforeUnmount(() => {
  cancelCloseProductDropdownTimer();
  cancelCloseUserDropdownTimer();
});
</script>

<style scoped>
.nav-link.dropdown-toggle {
  cursor: pointer;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 1030;
  /* Sử dụng biến CSS cho màu nền và màu viền (nếu có) */
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--header-border); /* Thêm viền nếu muốn */
  /* Giữ lại transition cho các thuộc tính cần thiết */
  transition: margin-left 0.3s ease, background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;
}

.logo-img {
  height: 40px;
  width: auto;
}

.header-search-desktop {
  flex-grow: 1;
  max-width: 450px;
}

/* Active state for nav links */
.navbar-nav .nav-link.active,
.navbar-nav .nav-link.router-link-exact-active {
  font-weight: 600;
  color: var(--bs-primary); /* Sử dụng biến Bootstrap */
  background-color: rgba(var(--bs-primary-rgb), 0.05); /* Sử dụng biến Bootstrap RGB */
}
/* Specific active state for dropdown items */
.dropdown-item.active,
.dropdown-item.router-link-active {
  font-weight: 600;
  background-color: rgba(var(--bs-primary-rgb), 0.1); /* Sử dụng biến Bootstrap RGB */
  color: var(--bs-primary); /* Sử dụng biến Bootstrap */
}

/* Dropdown menu visibility */
.dropdown-menu {
  transition: opacity 0.15s linear;
  opacity: 0;
  visibility: hidden;
  position: absolute;
  z-index: 1000;
  margin-top: 0.5rem !important;
  /* Sử dụng biến CSS cho nền, viền, bóng đổ */
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  /* box-shadow: var(--bs-box-shadow-sm); */ /* Sử dụng bóng Bootstrap nếu cần */
  border-radius: var(--border-radius-lg, 0.5rem);
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
}
.dropdown-menu.show {
  opacity: 1;
  visibility: visible;
}

.dropdown-menu .dropdown-item {
  color: var(--text-primary); /* Màu chữ chính */
  padding: 0.5rem 1.25rem;
  transition: background-color 0.2s ease, color 0.2s ease;
  display: block;
  width: 100%;
  clear: both;
  font-weight: 400;
  text-align: inherit;
  white-space: nowrap;
  background-color: transparent;
  border: 0;
}

.dropdown-menu .dropdown-item:hover,
.dropdown-menu .dropdown-item:focus {
  background-color: var(--bg-accent); /* Nền hover */
  color: var(--bs-primary); /* Màu chữ primary khi hover */
}

.dropdown-menu .dropdown-item.disabled {
  cursor: default;
  opacity: 0.7;
  background-color: transparent;
  /* Màu chữ sẽ được xử lý bởi class text-muted hoặc text-danger */
}

.dropdown-divider {
  border-top-color: var(--border-color-subtle); /* Viền mờ hơn */
  margin: 0.5rem 0;
}

.navbar-nav .btn-sm {
  padding: 0.25rem 0.6rem;
  font-size: 0.875rem;
}

.nav-link i,
.dropdown-item i {
  vertical-align: -0.125em;
  margin-right: 0.4rem;
}
.dropdown-item i {
  width: 1.3em;
  text-align: center;
  margin-right: 0.5rem;
}
/* Màu icon danger sẽ được xử lý bởi class text-danger */
.dropdown-item.text-danger i {
  /* color: var(--bs-danger); */ /* Không cần ghi đè ở đây nếu class text-danger hoạt động */
}

.dropdown-menu-end {
  right: 0;
  left: auto;
}

.cart-badge {
  font-size: 0.65em;
  padding: 0.3em 0.55em;
  background-color: var(--bs-danger); /* Màu nền danger từ Bootstrap */
  color: var(--text-on-primary-bg); /* Màu chữ trắng (hoặc tương phản) */
  border: 1px solid var(--bg-primary); /* Viền cùng màu nền header/dropdown */
}

.navbar {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.navbar-brand {
  color: var(--text-primary); /* Màu chữ chính */
  font-weight: 700 !important;
  font-size: 1.5rem;
  transition: color 0.2s ease;
}
.navbar-brand:hover {
  color: var(--bs-primary); /* Màu primary khi hover */
}

.navbar-nav .nav-link {
  color: var(--text-secondary); /* Màu chữ phụ */
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius-md, 0.375rem);
  transition: color 0.2s ease, background-color 0.2s ease;
  position: relative;
}

.navbar-nav .nav-link:hover {
  /* Hover style for non-active links */
  color: var(--bs-primary); /* Màu primary khi hover */
  background-color: rgba(var(--bs-primary-rgb), 0.05); /* Nền nhẹ màu primary */
}

/* User/Cart icons refinement */
.navbar-nav .nav-link .bi {
  font-size: 1.3rem;
  vertical-align: middle;
}
.navbar-nav .nav-link.position-relative .badge {
  border: 2px solid var(--bg-primary); /* Cập nhật viền badge theo nền */
  /* Các thuộc tính khác đã định nghĩa ở .cart-badge */
}

a.dropdown-toggle {
  font-weight: 500;
}
a.dropdown-toggle .bi-person-circle {
  margin-right: 0.5rem !important;
}

/* Nút đăng ký */
.navbar-nav .btn-outline-primary {
  border-color: var(--bs-primary);
  color: var(--bs-primary);
  transition: all 0.2s ease;
}
.navbar-nav .btn-outline-primary:hover {
  background-color: var(--bs-primary);
  color: var(--text-on-primary-bg); /* Sử dụng biến cho màu chữ trên nền primary */
}

/* Mobile Adjustments */
@media (max-width: 991.98px) {
  .dropdown-menu {
    position: static !important;
    float: none !important;
    width: auto !important;
    margin-top: 0 !important;
    background-color: transparent !important; /* Nền trong suốt trên mobile */
    border: 0 !important;
    box-shadow: none !important;
    padding-left: 0.5rem;
    opacity: 1;
    visibility: visible;
    display: none;
  }
  .dropdown-menu.show {
    display: block;
  }
  .dropdown-item {
    padding-left: 1.5rem;
    /* Màu chữ và nền hover đã được định nghĩa ở trên */
  }
  .dropdown-divider {
    margin: 0.5rem 1rem;
    border-top-color: var(--border-color-subtle); /* Sử dụng biến viền mờ */
  }
  .navbar-nav .btn-sm {
    margin-left: 0;
    margin-top: 0.5rem;
    display: inline-block;
    width: auto;
  }
  .header-actions .nav-item {
    margin-top: 0.5rem;
  }
  .header-actions .nav-item:first-child {
    margin-top: 0.75rem;
  }
  .header-actions .ms-lg-3 {
    margin-left: 0 !important;
  }
  .navbar-nav > .nav-item.ms-lg-2,
  .navbar-nav > .nav-item.ms-lg-3 {
    margin-left: 0 !important;
  }
}

/* Fixed Sidebar Toggle Button */
.fixed-sidebar-toggle {
  position: fixed;
  top: 15px;
  left: 25px;
  z-index: 1035;
  font-weight: 500;
  border: 0px;
  /* Màu nền và chữ sẽ lấy theo biến --text-secondary và --bg-primary (hoặc --bg-secondary) */
  color: var(--text-secondary);
  background-color: var(--bg-primary); /* Hoặc --bg-secondary nếu muốn khác biệt */
  padding: 0.3rem 0.8rem;
  border-radius: var(--border-radius-sm, 0.25rem);
  transition: background-color 0.2s ease, color 0.2s ease; /* Thêm transition cho color */
  box-shadow: var(--bs-box-shadow-sm); /* Thêm bóng đổ nhẹ */
}
.fixed-sidebar-toggle:hover {
  background-color: var(--bg-accent); /* Màu nền khi hover */
  color: var(--text-primary); /* Màu chữ khi hover */
}

.fixed-sidebar-toggle .bi-list {
  font-size: 1.5rem;
  vertical-align: middle;
}

.fixed-sidebar-toggle span {
  font-size: 0.95rem;
  vertical-align: middle;
}
</style>
