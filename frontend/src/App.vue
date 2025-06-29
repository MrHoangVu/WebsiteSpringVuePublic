<template>
  <div id="app">
    <AppHeader :class="{ 'shifted-by-sidebar': isSidebarOpen && isLargeScreen }"/>
    <AppSidebar/>
    <main
      class="main-content"
      :class="{ 'shifted-by-sidebar': isSidebarOpen && isLargeScreen }"
    >
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component"/>
        </transition>
      </router-view>
    </main>
    <AppFooter class="app-footer" :class="{ 'shifted-by-sidebar': isSidebarOpen && isLargeScreen }"/>
    <ZaloLinkWidget />
    <ChatWidget v-if="!isAdminRoute"/>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRoute } from 'vue-router';
import AppHeader from "./components/layout/AppHeader.vue";
import AppSidebar from "./components/layout/AppSidebar.vue";
import AppFooter from "./components/layout/AppFooter.vue";
import ChatWidget from "@/components/chat/ChatWidget.vue";
import ZaloLinkWidget from '@/components/chat/ZaloLinkWidget.vue';
import { Offcanvas } from "bootstrap";
import { useUiStore } from '@/store/ui';

const uiStore = useUiStore();
const route = useRoute();

const isLargeScreen = ref(false);
const isSidebarOpen = ref(false);
// const sidebarWidth = 280; // Không còn sử dụng trực tiếp trong script này
let mediaQueryList = null;
let sidebarInstance = null;

const checkScreenSize = () => {
  isLargeScreen.value = mediaQueryList.matches;
  // Logic này có vẻ không cần thiết nữa nếu CSS xử lý đúng
  // if (!isLargeScreen.value && sidebarInstance && !sidebarInstance._isShown) {
  //   isSidebarOpen.value = false;
  // }
  // if (isLargeScreen.value && sidebarInstance && !sidebarInstance._isShown) {
  //   isSidebarOpen.value = false;
  // }
};
const handleSidebarShow = () => { isSidebarOpen.value = true; };
const handleSidebarHide = () => { isSidebarOpen.value = false; };

const isAdminRoute = computed(() => route.matched.some(record => record.path.startsWith('/admin')));

onMounted(() => {
  uiStore.initializeTheme(); // Khởi tạo theme đã lưu hoặc theo hệ thống

  mediaQueryList = window.matchMedia("(min-width: 992px)");
  checkScreenSize();
  mediaQueryList.addEventListener("change", checkScreenSize);
  const sidebarElement = document.getElementById("appSidebar");
  if (sidebarElement) {
    sidebarInstance = Offcanvas.getOrCreateInstance(sidebarElement);
    sidebarElement.addEventListener('show.bs.offcanvas', handleSidebarShow);
    sidebarElement.addEventListener('hide.bs.offcanvas', handleSidebarHide);
    // Cập nhật trạng thái ban đầu nếu sidebar đã hiển thị sẵn (trên desktop)
    if (sidebarInstance._isShown) { isSidebarOpen.value = true; }
  } else { console.error("AppSidebar element not found"); }
});

onUnmounted(() => {
  if (mediaQueryList) { mediaQueryList.removeEventListener("change", checkScreenSize); }
  const sidebarElement = document.getElementById("appSidebar");
  if (sidebarElement && sidebarInstance) { // Kiểm tra sidebarInstance tồn tại
    sidebarElement.removeEventListener('show.bs.offcanvas', handleSidebarShow);
    sidebarElement.removeEventListener('hide.bs.offcanvas', handleSidebarHide);
    // Không dispose ở đây trừ khi App.vue bị unmount hoàn toàn
  }
});
</script>

<style>
/* --- Global Styles --- */
body {
  background-color: var(--bg-primary); /* Nền chính của trang */
  color: var(--text-primary); /* Màu chữ mặc định */
  /* Thêm transition để chuyển màu nền mượt hơn */
  transition: background-color 0.3s ease, color 0.3s ease;
  /* Các thuộc tính font giữ nguyên nếu có */
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* === Transitions & Shifting === */
.main-content,
.app-footer,
.app-header { /* Thêm app-header vào đây để đồng bộ */
  transition: margin-left 0.3s ease;
}

/* Page transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease; /* Thời gian transition ngắn hơn */
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* === Desktop Styles === */
@media (min-width: 992px) {
  .main-content.shifted-by-sidebar,
  .app-header.shifted-by-sidebar,
  .app-footer.shifted-by-sidebar {
    margin-left: 280px; /* Bằng chiều rộng sidebar, đảm bảo giá trị này khớp với CSS của sidebar */
  }

  .offcanvas-backdrop {
    display: none !important;
  }

  #appSidebar.offcanvas.show {
    visibility: visible !important;
  }
}

/* === Mobile Styles === */
@media (max-width: 991.98px) {
  .main-content.shifted-by-sidebar,
  .app-header.shifted-by-sidebar,
  .app-footer.shifted-by-sidebar {
    margin-left: 0; /* Reset margin trên mobile */
  }
  /* Trên mobile, main-content có thể bị che bởi header sticky, cần padding-top */
  .main-content {
    /* Giá trị này nên bằng chiều cao của header */
    /* Bạn cần xác định chiều cao header hoặc dùng JS để lấy */
    /* Ví dụ: padding-top: 70px; */
    /* Hoặc tốt hơn là đặt padding này trong component Header hoặc CSS global khác */
  }
}
</style>
