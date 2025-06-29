<template>
  <div class="col">
    <div class="card h-100 shadow-sm article-card">
      <router-link :to="{ name: 'articleDetail', params: { slug: article.slug } }" class="text-decoration-none text-dark">
        <img
          v-if="article.featuredImageUrl"
          :src="article.featuredImageUrl"
          class="card-img-top article-image"
          :alt="article.title"
          @error="onImageError"
        />
        <!-- Ảnh placeholder nếu không có ảnh đại diện -->
        <img
          v-else
          src="@/assets/images/placeholder.png"
          class="card-img-top article-image placeholder-image"
          alt="Placeholder"
        />
        <div class="card-body d-flex flex-column">
          <h5 class="card-title article-title flex-grow-1">{{ article.title }}</h5>
          <p v-if="article.excerpt" class="card-text article-excerpt small text-muted">{{ truncateText(article.excerpt, 100) }}</p>
          <div class="mt-auto d-flex justify-content-between align-items-center small text-muted">
             <span v-if="article.authorUsername">
                <i class="bi bi-person"></i> {{ article.authorUsername }}
             </span>
            <span v-if="article.publishedAt || article.createdAt">
                <i class="bi bi-calendar3"></i> {{ formatDate(article.publishedAt || article.createdAt) }}
             </span>
          </div>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { RouterLink } from 'vue-router';
import placeholderImage from '@/assets/images/placeholder.png'; // Import ảnh placeholder

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
});

// Hàm xử lý lỗi ảnh
const onImageError = (event) => {
  event.target.src = placeholderImage;
  event.target.classList.add('placeholder-image'); // Thêm class để có thể style khác nếu muốn
};

// Hàm rút gọn text
const truncateText = (text, length) => {
  if (!text) return '';
  if (text.length <= length) return text;
  return text.substring(0, length) + '...';
};

// Hàm định dạng ngày
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch (e) {
    return dateString; // Trả về chuỗi gốc nếu lỗi
  }
};
</script>

<style scoped>
.article-card {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1) !important;
}
.article-image {
  height: 180px;
  object-fit: cover;
}
.placeholder-image {
  object-fit: contain; /* Hiển thị placeholder tốt hơn */
  opacity: 0.7;
}
.article-title {
  font-size: 1.1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 2.6em; /* Chỗ cho 2 dòng */
  margin-bottom: 0.5rem;
}
.article-excerpt {
  font-size: 0.85rem;
  line-height: 1.4;
  margin-bottom: 0.75rem;
}
.card-body {
  padding: 0.8rem;
}
.card-body .small i {
  vertical-align: -0.1em;
}
</style>
