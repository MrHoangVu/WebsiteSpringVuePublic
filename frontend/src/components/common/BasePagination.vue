<template>
  <nav aria-label="Page navigation" v-if="totalPages > 1">
    <ul class="pagination justify-content-center">
      <!-- Nút Trang Trước -->
      <li class="page-item" :class="{ disabled: currentPage === 0 }">
        <a class="page-link" href="#" aria-label="Previous" @click.prevent="changePage(currentPage - 1)">
          <span aria-hidden="true">«</span>
        </a>
      </li>

      <!-- Các nút số trang -->
      <li
        v-for="pageNumber in visiblePageNumbers"
        :key="pageNumber"
        class="page-item"
        :class="{ active: pageNumber - 1 === currentPage, disabled: pageNumber === '...' }"
      >
        <a class="page-link" href="#" @click.prevent="changePage(pageNumber - 1)" v-if="pageNumber !== '...'">
          {{ pageNumber }}
        </a>
        <span class="page-link" v-else>...</span>
      </li>

      <!-- Nút Trang Sau -->
      <li class="page-item" :class="{ disabled: currentPage >= totalPages - 1 }">
        <a class="page-link" href="#" aria-label="Next" @click.prevent="changePage(currentPage + 1)">
          <span aria-hidden="true">»</span>
        </a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  currentPage: { // Trang hiện tại (index từ 0)
    type: Number,
    required: true
  },
  totalPages: { // Tổng số trang
    type: Number,
    required: true
  },
  maxVisibleButtons: { // Số nút trang tối đa hiển thị (không tính prev/next)
    type: Number,
    default: 5
  }
});

const emit = defineEmits(['page-change']); // Event để thông báo thay đổi trang

const changePage = (page) => {
  // Đảm bảo không đi ra ngoài phạm vi trang và không emit khi click vào trang hiện tại hoặc '...'
  if (page >= 0 && page < props.totalPages && page !== props.currentPage) {
    emit('page-change', page); // Gửi trang mới (index từ 0)
  }
};

// Logic tính toán các nút trang hiển thị (có dấu '...')
const visiblePageNumbers = computed(() => {
  const total = props.totalPages;
  const current = props.currentPage + 1; // Chuyển sang index từ 1 để dễ tính toán
  const maxVisible = props.maxVisibleButtons;
  const halfMax = Math.floor(maxVisible / 2);

  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  let startPage = Math.max(1, current - halfMax);
  let endPage = Math.min(total, current + halfMax);

  // Điều chỉnh nếu gần đầu hoặc cuối
  if (current - halfMax <= 1) {
    endPage = maxVisible;
  }
  if (current + halfMax >= total) {
    startPage = total - maxVisible + 1;
  }
  // Đảm bảo số lượng nút đúng bằng maxVisible khi total lớn
  if(endPage - startPage + 1 < maxVisible && total > maxVisible){
    if(startPage === 1) endPage = maxVisible;
    else startPage = total - maxVisible + 1;
  }


  const pages = [];
  if (startPage > 1) {
    pages.push(1);
    if (startPage > 2) {
      pages.push('...');
    }
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }

  if (endPage < total) {
    if (endPage < total - 1) {
      pages.push('...');
    }
    pages.push(total);
  }

  return pages;
});

</script>

<style scoped>
.pagination {
  margin-top: 2rem;
}
/* Có thể thêm style tùy chỉnh nếu cần */
.page-item.disabled .page-link {
  cursor: not-allowed;
  /* Bootstrap đã xử lý màu sắc */
}
.page-item.active .page-link {
  z-index: 3; /* Đảm bảo active link nằm trên */
}
.page-item span.page-link { /* Style cho dấu '...' */
  color: #6c757d;
  pointer-events: none;
}
</style>
