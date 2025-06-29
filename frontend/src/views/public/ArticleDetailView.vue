<template>
  <div class="article-detail-view container mt-4 mb-5">
    <div v-if="loading" class="d-flex justify-content-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      <p>Lỗi: {{ error }}</p>
      <router-link :to="{ name: 'articleList' }">Quay lại danh sách bài viết</router-link>
    </div>

    <div v-else-if="article" class="article-content">
      <!-- Tiêu đề -->
      <h1 class="mb-3 display-5">{{ article.title }}</h1>

      <!-- Thông tin tác giả, ngày đăng -->
      <div class="text-muted mb-4 article-meta">
        <span v-if="article.authorFullName || article.authorUsername">
          <i class="bi bi-person-fill"></i> Tác giả:
          {{ article.authorFullName || article.authorUsername }}
        </span>
        <span class="mx-2" v-if="article.authorUsername && article.publishedAt">|</span>
        <span v-if="article.publishedAt">
          <i class="bi bi-calendar-check"></i> Ngày đăng: {{ formatDate(article.publishedAt) }}
        </span>
      </div>

      <!-- Ảnh đại diện -->
      <img
        v-if="article.featuredImageUrl"
        :src="article.featuredImageUrl"
        class="img-fluid rounded mb-4 article-featured-image"
        :alt="article.title"
        @error="onImageError"
      />

      <!-- Nội dung bài viết -->
      <div class="article-body" v-html="article.content"></div>
      <!-- HOẶC nếu content là plain text: -->
      <!-- <div class="article-body" style="white-space: pre-wrap;">{{ article.content }}</div> -->
    </div>

    <div v-else class="alert alert-info text-center">
      Không tìm thấy bài viết.
      <router-link :to="{ name: 'articleList' }">Quay lại danh sách</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { getArticleBySlug } from "@/http/modules/public/articleService.js";
import placeholderImage from "@/assets/images/placeholder.png";

const route = useRoute();
const router = useRouter(); // Có thể dùng để điều hướng nếu lỗi

const article = ref(null);
const loading = ref(true);
const error = ref(null);

const fetchArticle = async (slug) => {
  if (!slug) {
    error.value = "Không tìm thấy slug bài viết.";
    loading.value = false;
    return;
  }
  loading.value = true;
  error.value = null;
  article.value = null; // Clear old data
  try {
    const response = await getArticleBySlug(slug);
    article.value = response.data;
  } catch (err) {
    console.error("Error fetching article by slug:", err);
    if (err.response && err.response.status === 404) {
      error.value = "Bài viết không tồn tại.";
    } else {
      error.value = "Không thể tải chi tiết bài viết.";
    }
  } finally {
    loading.value = false;
  }
};

// Hàm định dạng ngày
const formatDate = (dateString) => {
  if (!dateString) return "";
  try {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (e) {
    return dateString;
  }
};

// Xử lý lỗi ảnh
const onImageError = (event) => {
  event.target.style.display = "none"; // Ẩn ảnh nếu lỗi
};

// Watch slug thay đổi và fetch lại
watch(
  () => route.params.slug,
  (newSlug) => {
    fetchArticle(newSlug);
  },
  { immediate: true } // Fetch ngay lần đầu
);
</script>

<style scoped>
.article-meta {
  font-size: 0.9rem;
}
.article-meta i {
  vertical-align: -0.1em;
  margin-right: 0.25rem;
}
.article-featured-image {
  max-height: 450px;
  width: 100%;
  object-fit: cover;
}
.article-body {
  line-height: 1.7;
  font-size: 1.1rem;
}
/* Style cho nội dung HTML từ v-html nếu cần */
.article-body :deep(p) {
  margin-bottom: 1.2em;
}
.article-body :deep(h2),
.article-body :deep(h3) {
  margin-top: 1.8em;
  margin-bottom: 0.8em;
  font-weight: 600;
}
.article-body :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 1em 0;
  border-radius: 4px;
}
.article-body :deep(a) {
  color: var(--bs-link-color);
  text-decoration: underline;
}
.article-body :deep(a:hover) {
  color: var(--bs-link-hover-color);
}
</style>
