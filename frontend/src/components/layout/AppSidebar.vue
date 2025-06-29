<template>
  <div
    class="offcanvas offcanvas-start text-bg-primary sidebar-container"
    tabindex="-1"
    id="appSidebar"
    aria-labelledby="appSidebarLabel"
    data-bs-scroll="true"
    data-bs-backdrop="true"
    data-bs-touch="false"
  >
    <div class="offcanvas-header">
      <h5 class="offcanvas-title" id="appSidebarLabel">
        <router-link
          :to="{ name: 'home' }"
          class="text-white text-decoration-none fw-bold sidebar-brand"
        >
          <img
            class="img-logosidebar"
            src="https://doanhnhantredaklak.org/userfiles/users/107/1609494305008.png"
            alt=""
          />
          Tượng Gỗ Phong Thủy
        </router-link>
      </h5>
      <button
        type="button"
        class="btn-close btn-close-white"
        data-bs-dismiss="offcanvas"
        aria-label="Đóng menu"
      ></button>
    </div>
    <div class="offcanvas-body d-flex flex-column">
      <!-- Navigation Links -->
      <ul class="nav nav-pills flex-column mb-auto sidebar-nav">
        <li class="nav-item">
          <router-link class="nav-link" :to="{ name: 'home' }" active-class="active">
            <i class="bi bi-house-door nav-icon"></i>
            <span>Trang chủ</span>
          </router-link>
        </li>

        <!-- Product Dropdown (Vue Controlled) -->
        <li class="nav-item">
          <a
            class="nav-link has-submenu"
            href="#"
            @click.prevent="toggleProductSubmenu"
            :class="{ active: isProductSubmenuRouteActive, open: isProductSubmenuOpen }"
            role="button"
            :aria-expanded="isProductSubmenuOpen"
            aria-controls="productSubmenuVue"
          >
            <i class="bi bi-tags nav-icon"></i>
            <span class="product-label">Sản phẩm</span>
            <i class="bi bi-chevron-right submenu-arrow"></i>
          </a>
          <!-- Smooth transition for submenu -->
          <div
            class="collapse-transition"
            :style="{ '--max-height': isProductSubmenuOpen ? productSubmenuHeight : '0px' }"
          >
            <div ref="productSubmenuContent" class="submenu-wrapper" id="productSubmenuVue">
              <ul class="nav flex-column small">
                <li class="nav-item-for-product">
                  <router-link
                    class="nav-link submenu-link"
                    :to="{ name: 'productList' }"
                    active-class="active"
                  >
                    Tất cả sản phẩm
                  </router-link>
                </li>
                <!-- Show limited categories -->
                <li
                  v-for="category in categories.slice(0, categoriesToShow)"
                  :key="'side-' + category.id"
                  class="nav-item-for-product"
                >
                  <router-link
                    class="nav-link submenu-link"
                    :to="{ name: 'productListByCategory', params: { categorySlug: category.slug } }"
                    active-class="active"
                  >
                    {{ category.name }}
                  </router-link>
                </li>
                <!-- Show More/Less Toggle -->
                <li v-if="categories.length > 5" class="nav-item">
                  <button
                    class="nav-link submenu-link text-start w-100 bg-transparent border-0"
                    @click="toggleShowMoreCategories"
                  >
                    <em>{{ categoriesToShow === 5 ? "Xem thêm..." : "Thu gọn..." }}</em>
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </li>

        <li class="nav-item">
          <router-link class="nav-link" :to="{ name: 'articleList' }" active-class="active">
            <i class="bi bi-newspaper nav-icon"></i>
            <span>Tin Tức</span>
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" :to="{ name: 'contact' }" active-class="active">
            <i class="bi bi-telephone nav-icon"></i>
            <span>Liên hệ</span>
          </router-link>
        </li>
      </ul>

      <!-- Divider -->
      <hr class="sidebar-divider" />

      <!-- User/Auth Section -->
      <div class="user-section">
        <!-- Authenticated View (Vue Controlled Dropdown) -->
        <div v-if="authStore.isAuthenticated" class="user-dropdown-wrapper">
          <a
            href="#"
            class="d-flex align-items-center text-white text-decoration-none user-dropdown-toggle"
            @click.prevent="toggleUserMenu"
            role="button"
            :aria-expanded="isUserMenuOpen"
            aria-controls="userMenuVue"
          >
            <i class="bi bi-person-circle user-avatar me-2"></i>
            <strong class="user-name">{{ authStore.user?.username || "Tài khoản" }}</strong>
            <i class="bi bi-chevron-down user-arrow ms-auto"></i>
          </a>
          <!-- Smooth transition for user menu -->
          <div
            class="collapse-transition"
            :style="{ '--max-height': isUserMenuOpen ? userMenuHeight : '0px' }"
          >
            <ul ref="userMenuContent" id="userMenuVue" class="list-unstyled user-dropdown-menu">
              <template v-if="authStore.isAdmin">
                <li>
                  <router-link class="user-dropdown-item" :to="{ name: 'adminDashboard' }">
                    <i class="bi bi-speedometer2 item-icon"></i><span>Thống kê</span>
                  </router-link>
                </li>
                <li>
                  <router-link class="user-dropdown-item" :to="{ name: 'adminOrderList' }">
                    <i class="bi bi-box-seam item-icon"></i><span>Đơn hàng</span>
                  </router-link>
                </li>
                <li>
                  <router-link class="user-dropdown-item" :to="{ name: 'adminProductList' }">
                    <i class="bi bi-tags item-icon"></i><span>Sản phẩm</span>
                  </router-link>
                </li>
                <li>
                  <hr class="user-dropdown-divider" />
                </li>
              </template>
              <template v-else>
                <!-- Regular User -->
                <li>
                  <router-link class="user-dropdown-item" :to="{ name: 'orderHistory' }">
                    <i class="bi bi-receipt item-icon"></i><span>Đơn hàng của tôi</span>
                  </router-link>
                </li>
              </template>
              <!-- Common for all logged-in users -->
              <li>
                <router-link class="user-dropdown-item" :to="{ name: 'userProfile' }">
                  <i class="bi bi-person-lines-fill item-icon"></i><span>Thông tin tài khoản</span>
                </router-link>
              </li>
              <li>
                <hr class="user-dropdown-divider" />
              </li>
              <li>
                <button
                  class="user-dropdown-item text-danger w-100"
                  type="button"
                  @click="handleLogoutAndCloseSidebar"
                >
                  <i class="bi bi-box-arrow-right item-icon"></i><span>Đăng xuất</span>
                </button>
              </li>
            </ul>
          </div>
        </div>
        <!-- Guest View -->
        <div v-else class="guest-actions">
          <router-link :to="{ name: 'login' }" class="btn btn-outline-light btn-sm me-2"
            >Đăng nhập
          </router-link>
          <router-link :to="{ name: 'register' }" class="btn btn-warning btn-sm"
            >Đăng ký
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick, onBeforeUnmount } from "vue";
import { RouterLink, useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { getAllCategories } from "@/http/modules/public/categoryService.js";
import { Offcanvas } from "bootstrap"; // Đảm bảo đã import

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const categories = ref([]);
const sidebarInstance = ref(null); // Giữ lại ref để dispose

// --- State for Vue-controlled Dropdowns ---
const isProductSubmenuOpen = ref(false);
const isUserMenuOpen = ref(false);

// --- Refs for height calculation ---
const productSubmenuContent = ref(null);
const userMenuContent = ref(null);
const productSubmenuHeight = ref("0px");
const userMenuHeight = ref("0px");

// --- Category Display Logic ---
const categoriesToShow = ref(5); // Initial number of categories to show

const toggleShowMoreCategories = () => {
  categoriesToShow.value = categoriesToShow.value === 5 ? categories.value.length : 5;
  // Recalculate height after changing content
  nextTick(() => {
    calculateProductSubmenuHeight();
  });
};

// --- Fetch Categories ---
const fetchCategories = async () => {
  try {
    const response = await getAllCategories();
    categories.value = response.data || [];
  } catch (err) {
    console.error("Sidebar: Error fetching categories:", err);
  }
};

// --- Sidebar Instance Management ---
// Hàm get instance, tạo nếu chưa có
const getSidebarInstance = () => {
  if (!sidebarInstance.value) {
    const sidebarElement = document.getElementById("appSidebar");
    if (sidebarElement) {
      // Use getOrCreateInstance to avoid issues if initialized elsewhere potentially
      sidebarInstance.value = Offcanvas.getOrCreateInstance(sidebarElement);
    }
  }
  return sidebarInstance.value;
};

// Hàm đóng sidebar (chỉ dùng cho logout hoặc các trường hợp đặc biệt khác nếu cần)
const closeSidebar = () => {
  isProductSubmenuOpen.value = false; // Close Vue-controlled menus
  isUserMenuOpen.value = false;

  const instance = getSidebarInstance(); // Lấy instance hiện tại hoặc tạo nếu chưa có
  // Check if instance exists and Bootstrap thinks it's shown
  // Note: instance._isShown is an internal property, might change.
  // Relying on instance.hide() to handle the check internally is safer.
  if (instance) {
    const sidebarElement = document.getElementById("appSidebar");
    // Double check if element has 'show' class, as instance might exist but be hidden
    if (sidebarElement && sidebarElement.classList.contains("show")) {
      instance.hide();
    }
  } else {
    console.warn("Sidebar instance not found, couldn't close programmatically.");
  }
};

// --- Height Calculation for Smooth Collapse ---
const calculateProductSubmenuHeight = () => {
  if (productSubmenuContent.value) {
    productSubmenuHeight.value = `${productSubmenuContent.value.scrollHeight}px`;
  }
};

const calculateUserMenuHeight = () => {
  if (userMenuContent.value) {
    userMenuHeight.value = `${userMenuContent.value.scrollHeight}px`;
  }
};

// --- Dropdown Toggle Functions ---
const toggleProductSubmenu = () => {
  isProductSubmenuOpen.value = !isProductSubmenuOpen.value;
  if (isProductSubmenuOpen.value) {
    nextTick(calculateProductSubmenuHeight); // Calculate height when opening
  }
};

const toggleUserMenu = () => {
  isUserMenuOpen.value = !isUserMenuOpen.value;
  if (isUserMenuOpen.value) {
    nextTick(calculateUserMenuHeight); // Calculate height when opening
  }
};

// --- Logout Handler ---
const handleLogoutAndCloseSidebar = () => {
  closeSidebar(); // Vẫn đóng khi logout
  authStore.logout();
  router.push({ name: "home" }).catch(() => {}); // Navigate home after logout
};

// --- Computed property for active product menu ---
const isProductSubmenuRouteActive = computed(() => {
  return route.name === "productList" || route.name === "productListByCategory";
});

// --- Watchers ---
watch(route, () => {
  // Close Vue-controlled menus on route change if they weren't the cause
  // Do NOT programmatically close the main sidebar here anymore
  //remove the following lines if you want to keep the sidebar open
  // if (isProductSubmenuOpen.value) isProductSubmenuOpen.value = false;
  // if (isUserMenuOpen.value) isUserMenuOpen.value = false;
});

// Watch for authentication changes to potentially recalculate user menu height
watch(
  () => authStore.isAuthenticated,
  (newVal, oldVal) => {
    if (newVal !== oldVal) {
      // If user menu might be open when auth state changes, recalculate its potential height
      if (isUserMenuOpen.value) {
        nextTick(calculateUserMenuHeight);
      }
    }
  }
);

// Define resize handler separately for proper removal
const handleResize = () => {
  if (isProductSubmenuOpen.value) calculateProductSubmenuHeight();
  if (isUserMenuOpen.value) calculateUserMenuHeight();
};

onMounted(async () => {
  await fetchCategories();
  getSidebarInstance(); // Initialize or get the instance on mount
  calculateProductSubmenuHeight(); // Calculate initial heights
  calculateUserMenuHeight();

  // Listener for when Bootstrap finishes hiding the offcanvas (good for resetting state)
  const sidebarElement = document.getElementById("appSidebar");
  if (sidebarElement) {
    sidebarElement.addEventListener("hidden.bs.offcanvas", () => {
      isProductSubmenuOpen.value = false;
      isUserMenuOpen.value = false;
    });
  }

  // Add resize listener
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  // Dọn dẹp listener resize
  window.removeEventListener("resize", handleResize);

  // Quan trọng: Dọn dẹp instance Bootstrap khi component bị hủy
  const instance = sidebarInstance.value; // Lấy từ ref
  if (instance) {
    instance.dispose(); // Hủy instance Bootstrap
    console.log("Sidebar instance disposed."); // Optional: for debugging
  }
  sidebarInstance.value = null; // Reset ref

  // Clean up Bootstrap event listener manually if needed, although dispose should handle it
  const sidebarElement = document.getElementById("appSidebar");
  if (sidebarElement) {
    // Removing the specific listener added in onMounted
    // Note: This assumes the listener function reference hasn't changed.
    // If anonymous functions were used, removal is harder.
    // Bootstrap's dispose() *should* handle its own event listeners.
    // Explicit removal might be redundant but safer in complex scenarios.
    // Example if hiddenListener was defined: sidebarElement.removeEventListener("hidden.bs.offcanvas", hiddenListener);
  }
});
</script>

<style scoped>
/* --- Base Offcanvas Styles --- */
.sidebar-container {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1045; /* Ensure it's above most content */
  background-color: var(--color-primary-dark, #4b2c20) !important; /* Darker primary */
  color: rgba(255, 255, 255, 0.85) !important; /* Softer white text */
  width: 280px !important;
  border-right: 1px solid rgba(255, 255, 255, 0.1); /* Subtle border */
  /* XÓA transition ở đây, để Bootstrap quản lý */
  /* transition: transform 0.3s ease-in-out; */
}

.offcanvas-header {
  padding: 1rem 1.25rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.1); /* Header divider */
  align-items: center;
}

/* Updated close button styles from suggestion */
.offcanvas-header .btn-close {
  padding: 0.5rem 0.5rem;
  margin: -0.5rem -0.5rem -0.5rem auto; /* Adjust margins for positioning */
  filter: invert(1) grayscale(100%) brightness(200%); /* Ensures white color */
  opacity: 0.7; /* Slightly transparent */
  transition: opacity 0.2s ease-in-out;
}
.offcanvas-header .btn-close:hover {
  opacity: 1; /* Fully opaque on hover */
}

.sidebar-brand {
  font-family: var(--font-family-heading, "Segoe UI", Tahoma, Geneva, Verdana, sans-serif);
  font-size: 1.25rem; /* Slightly smaller */
  font-weight: 600; /* Less bold */
}

.sidebar-brand i {
  vertical-align: -0.1em;
  color: var(--color-accent, #d4af37); /* Accent color for icon */
}

.offcanvas-body {
  padding: 0; /* Remove default body padding, handle with nav items */
  display: flex;
  flex-direction: column;
  /* Allow scrolling within the body if content overflows */
  overflow-y: auto;
  overflow-x: hidden; /* Prevent horizontal scroll */
}

/* --- Sidebar Navigation --- */
.sidebar-nav {
  padding: 0.5rem 0; /* Padding top/bottom for the nav list */
}

.sidebar-nav .nav-item {
  /* Add slight separation between items */
  margin-left: -10px;
  margin-bottom: 2px;
  border-bottom: 1px solid gray;
}

.sidebar-nav .nav-link {
  color: rgba(255, 255, 255, 0.8);
  padding: 0.8rem 1.25rem; /* Adjust padding */
  /* border-radius: 4px; Remove radius for full-width feel */
  /* margin: 0 0.5rem; Add horizontal margin for inset effect */
  transition: background-color 0.2s ease, color 0.2s ease, padding-left 0.3s ease;
  font-weight: 500;
  display: flex;
  align-items: center;
  position: relative; /* Needed for arrow */
  text-decoration: none;
  white-space: nowrap; /* Prevent text wrapping */
}

.sidebar-nav .nav-link .nav-icon {
  width: 1.25em; /* Fixed width for icon */
  font-size: 1.1rem;
  margin-right: 0.9rem;
  text-align: center;
  flex-shrink: 0; /* Prevent icon from shrinking */
  transition: color 0.3s ease;
}

.sidebar-nav .nav-link span {
  flex-grow: 1; /* Allow text to take remaining space */
}

.sidebar-nav .nav-link:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.sidebar-nav .nav-link.active {
  background-color: var(--color-accent, #d4af37);
  color: var(--color-primary-dark, #40241a);
  font-weight: 600;
}

.sidebar-nav .nav-link.active .nav-icon {
  color: var(--color-primary-dark, #40241a);
}

/* --- Submenu Styles --- */
.nav-link.has-submenu .submenu-arrow {
  margin-left: auto;
  transition: transform 0.3s ease;
  font-size: 0.75em;
  opacity: 0.7;
}

.nav-link.has-submenu.open .submenu-arrow {
  transform: rotate(90deg);
}

/* Highlight parent when submenu is open OR a child route is active */
.nav-link.has-submenu.open,
.nav-link.has-submenu.active {
  /* Combine open and active styles */
  background-color: rgba(255, 255, 255, 0.06);
  color: #fff;
}

/* Active state takes precedence for background */
.sidebar-nav .nav-link.has-submenu.active {
  background-color: var(--color-accent, #d4af37);
  color: var(--color-primary-dark, #40241a);
}

.sidebar-nav .nav-link.has-submenu.active .submenu-arrow {
  opacity: 1;
}

/* --- Smooth Collapse Transition --- */
.collapse-transition {
  overflow: hidden;
  transition: max-height 0.35s ease-in-out; /* Smooth transition */
  max-height: var(--max-height, 0px); /* Controlled by Vue */
}

.submenu-wrapper {
  padding: 0.3rem 0 0.5rem 0; /* Padding inside the submenu container */
  /* background-color: rgba(0, 0, 0, 0.1); Slightly darker background for submenu area */
}

.submenu-wrapper .nav-link.submenu-link {
  padding: 0.4rem 1.25rem 0.4rem 3rem; /* Indented padding */
  font-size: 0.88rem;
  color: rgba(255, 255, 255, 0.7);
  position: relative;
}

/* Subtle line indicator for active submenu item */
.submenu-wrapper .nav-link.submenu-link.active {
  color: var(--color-accent-light, #f0d882); /* Lighter accent for active sub-item */
  background-color: transparent;
  font-weight: 500;
}

.submenu-wrapper .nav-link.submenu-link.active::before {
  content: "";
  position: absolute;
  left: 1.25rem; /* Align with parent icon area */
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px; /* Adjust height */
  background-color: var(--color-accent-light, #f0d882);
  border-radius: 3px;
}

.submenu-wrapper .nav-link.submenu-link:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.05);
}

.submenu-wrapper .nav-item button.submenu-link {
  font-style: italic;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
}

.submenu-wrapper .nav-item button.submenu-link:hover {
  color: rgba(255, 255, 255, 0.9);
}

/* --- Divider --- */
.sidebar-divider {
  margin: 1rem 1.25rem; /* Match horizontal padding */
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* --- User Section --- */
.user-section {
  padding: 0 1.25rem 1rem 1.25rem; /* Padding around user area */
  margin-top: auto; /* Pushes to the bottom */
}

.user-dropdown-wrapper {
  position: relative;
}

.user-dropdown-toggle {
  cursor: pointer;
  padding: 0.75rem 0; /* Padding for the toggle area */
  display: flex !important;
  align-items: center !important;
  border-radius: var(--border-radius-sm, 0.25rem);
  transition: background-color 0.2s ease;
}

.user-dropdown-toggle:hover {
  background-color: rgba(255, 255, 255, 0.08);
}

.user-dropdown-toggle .user-avatar {
  font-size: 1.8rem; /* Larger avatar icon */
  opacity: 0.9;
}

.user-dropdown-toggle .user-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px; /* Prevent overly long names */
}

.user-dropdown-toggle .user-arrow {
  transition: transform 0.3s ease;
  font-size: 0.8em;
  opacity: 0.7;
}

/* Rotate arrow when open */
.user-dropdown-toggle[aria-expanded="true"] .user-arrow {
  transform: rotate(180deg);
}

.user-dropdown-toggle[aria-expanded="true"] {
  background-color: rgba(255, 255, 255, 0.08);
}

.user-dropdown-menu {
  /* background-color: rgba(0, 0, 0, 0.2); Darker background for menu */
  border-radius: var(--border-radius-md, 0.375rem);
  padding: 0.5rem 0;
  margin: 0; /* Remove default margin */
  list-style: none;
}

.user-dropdown-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0.6rem 1rem; /* Padding for items */
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.8);
  background-color: transparent;
  border-top: 0px;
  border-right: 0px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-left: 0px;
  text-align: left;
  text-decoration: none;
  transition: background-color 0.2s ease, color 0.2s ease;
  cursor: pointer;
  border-radius: var(--border-radius-sm, 0.25rem);
  white-space: nowrap;
}

.user-dropdown-item .item-icon {
  width: 1.25em; /* Fixed width */
  text-align: center;
  margin-right: 0.8rem;
  opacity: 0.8;
  flex-shrink: 0;
}

.user-dropdown-item span {
  flex-grow: 1;
}

.user-dropdown-item:hover,
.user-dropdown-item:focus {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  outline: none;
}

.user-dropdown-item.text-danger {
  color: #f8a5a5 !important; /* Lighter red for dark background */
}

.user-dropdown-item.text-danger .item-icon {
  color: #f8a5a5 !important;
  opacity: 1;
}

.user-dropdown-item.text-danger:hover {
  background-color: rgba(220, 53, 69, 0.2);
  color: #ffcdcd !important;
}

.user-dropdown-item.text-danger:hover .item-icon {
  color: #ffcdcd !important;
}

.user-dropdown-divider {
  height: 1px;
  margin: 0.5rem 1rem; /* Indent divider */
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.1);
  border: 0;
}

/* --- Guest Actions --- */
.guest-actions {
  padding: 0.75rem 0; /* Match user dropdown toggle padding */
  display: flex;
  justify-content: center; /* Center buttons */
}

.guest-actions .btn-sm {
  font-size: 0.85rem;
  padding: 0.4rem 0.8rem;
}

.guest-actions .btn-outline-light {
  border-color: rgba(255, 255, 255, 0.5);
  color: rgba(255, 255, 255, 0.8);
}

.guest-actions .btn-outline-light:hover {
  border-color: #fff;
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.guest-actions .btn-warning {
  color: #333; /* Darker text on warning */
}

/* --- Scrollbar styling (optional, for webkit browsers) --- */
.offcanvas-body::-webkit-scrollbar {
  width: 6px;
}

.offcanvas-body::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.offcanvas-body::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

.offcanvas-body::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.5);
}

.nav-item-for-product {
  margin-left: 20px;
}

.img-logosidebar {
  width: 20px; /* nhỏ giống icon (~16–24px) */
  height: 20px;
  object-fit: contain; /* giữ tỉ lệ ảnh, không bị méo */
  display: inline-block; /* để nằm cùng dòng với chữ/icon */
  vertical-align: middle; /* căn giữa theo chiều cao dòng */
  margin-right: 0.5rem; /* Add some spacing */
}
</style>
