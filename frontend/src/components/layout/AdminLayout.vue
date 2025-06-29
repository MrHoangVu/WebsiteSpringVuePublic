<template>
  <div class="admin-layout d-flex">
    <AdminSidebar />
    <main class="admin-main-content flex-grow-1">
      <!-- Có thể thêm AdminHeader ở đây nếu cần -->
      <!-- <AdminHeader /> -->
      <div class="admin-page-content p-3 p-lg-4">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
      <!-- Có thể thêm AdminFooter ở đây nếu cần -->
    </main>
  </div>
</template>

<script setup>
import AdminSidebar from './AdminSidebar.vue';
// import AdminHeader from './AdminHeader.vue'; // Uncomment nếu tạo AdminHeader
import { RouterView } from 'vue-router';
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-main-content {
  margin-left: 260px; /* Same as sidebar width */
  width: calc(100% - 260px); /* Take remaining width */
  transition: margin-left 0.3s ease, width 0.3s ease; /* Add transition if sidebar can collapse */
}

.admin-page-content {
  /* Add padding or other styles for the content area */
}

/* Page transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive: Adjust content margin when sidebar is hidden on smaller screens */
@media (max-width: 991.98px) {
  .admin-main-content {
    margin-left: 0;
    width: 100%;
  }
  /* Add styles for mobile admin header/toggle if you implement it */
}
</style>
