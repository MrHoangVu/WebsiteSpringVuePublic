<template>
  <!-- Loại bỏ bg-dark, text-white -->
  <nav id="adminSidebar" class="d-flex flex-column flex-shrink-0 p-3 admin-sidebar">
    <!-- Loại bỏ text-white -->
    <router-link
      :to="{ name: 'adminDashboard' }"
      class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-decoration-none sidebar-brand"
    >
      <img
        src="https://doanhnhantredaklak.org/userfiles/users/107/1609494305008.png"
        alt="Logo"
        class="sidebar-logo me-2"
      />
      <span class="fs-5">Admin Panel</span>
    </router-link>
    <hr class="sidebar-divider" />
    <ul class="nav nav-pills flex-column mb-auto sidebar-nav">
      <li class="nav-item">
        <!-- Loại bỏ text-white -->
        <router-link class="nav-link" :to="{ name: 'adminDashboard' }" active-class="active">
          <i class="bi bi-speedometer2 nav-icon"></i>
          <span>Thống kê</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" :to="{ name: 'adminOrderList' }" active-class="active">
          <i class="bi bi-box-seam nav-icon"></i>
          <span>Quản lý Đơn hàng</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" :to="{ name: 'adminProductList' }" active-class="active">
          <i class="bi bi-tags nav-icon"></i>
          <span>Quản lý Sản phẩm</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" :to="{ name: 'adminUserList' }" active-class="active">
          <i class="bi bi-people nav-icon"></i>
          <span>Quản lý Khách hàng</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link
          class="nav-link"
          :to="{ name: 'adminShippingMethodList' }"
          active-class="active"
        >
          <i class="bi bi-truck nav-icon"></i>
          <span>Quản lý Vận chuyển</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" :to="{ name: 'adminPromotionList' }" active-class="active">
          <i class="bi bi-gift nav-icon"></i>
          <span>Quản lý Khuyến mãi</span>
        </router-link>
      </li>
      <li class="nav-item">
        <router-link class="nav-link" :to="{ name: 'adminArticleList' }" active-class="active">
          <i class="bi bi-journal-text nav-icon"></i>
          <span>Quản lý Bài viết</span>
        </router-link>
      </li>
    </ul>
    <hr class="sidebar-divider" />
    <div class="admin-sidebar-footer">
      <!-- Thay btn-outline-light bằng btn-outline-secondary -->
      <router-link :to="{ name: 'home' }" class="btn btn-sm btn-outline-secondary w-100 mb-2">
        <i class="bi bi-shop me-1"></i> Xem Trang Bán Hàng
      </router-link>
      <!-- btn-danger sẽ tự đổi màu -->
      <button class="btn btn-sm btn-danger w-100" @click="handleLogout">
        <i class="bi bi-box-arrow-right me-1"></i> Đăng xuất
      </button>
    </div>
  </nav>
</template>

<script setup>
import { RouterLink, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push({ name: "home" });
};
</script>

<style scoped>
.admin-sidebar {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  width: 260px;
  /* Sử dụng biến CSS */
  background-color: var(--sidebar-bg);
  color: var(--sidebar-text);
  /* Sử dụng biến CSS cho shadow/border */
  box-shadow: inset -1px 0 0 var(--border-color-subtle);
  transition: background-color 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
}

.sidebar-brand {
  padding: 1rem 0.75rem;
  font-weight: 600;
  color: var(--sidebar-text); /* Đảm bảo màu chữ brand */
}

.sidebar-logo {
  height: 32px;
  width: auto;
}

.sidebar-divider {
  margin: 1rem 0;
  /* Sử dụng biến CSS */
  border-top: 1px solid var(--border-color-subtle);
}

.sidebar-nav .nav-link {
  padding: 0.65rem 1rem;
  font-size: 0.95rem;
  border-radius: 0.3rem;
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
  margin-bottom: 2px;
  /* Sử dụng biến CSS cho màu chữ link */
  color: var(--sidebar-text);
}

.sidebar-nav .nav-link .nav-icon {
  width: 1.25em;
  margin-right: 0.75rem;
  font-size: 1.1rem;
  vertical-align: text-bottom;
}

.sidebar-nav .nav-link:hover {
  /* Sử dụng màu nền hover dựa trên màu chữ */
  /* !!! Yêu cầu: Thêm --sidebar-text-rgb vào theme.css !!! */
  /* Ví dụ: --sidebar-text-rgb: 255, 255, 255; */
  background-color: rgba(var(--sidebar-text-rgb, 255, 255, 255), 0.08); /* Thêm fallback */
  color: var(--sidebar-text); /* Giữ nguyên màu chữ hoặc làm sáng nhẹ nếu cần */
}

.sidebar-nav .nav-link.active {
  /* Sử dụng biến CSS cho trạng thái active */
  background-color: var(--sidebar-link-active-bg);
  color: var(--sidebar-link-active-text);
  font-weight: 500;
}

.admin-sidebar-footer {
  padding: 1rem;
}

.admin-sidebar-footer .btn {
  font-size: 0.9rem;
}

@media (max-width: 991.98px) {
  .admin-sidebar {
    display: none; /* Giữ nguyên logic ẩn trên mobile */
  }
}
</style>
