<template>
  <div class="article-list-view container mt-4">
    <h1 class="text-center mb-4">Bài Viết & Tin Tức</h1>

    <div v-if="loading" class="d-flex justify-content-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      Lỗi tải bài viết: {{ error }}
    </div>

    <div v-else>
      <div v-if="articles.length > 0" class="row row-cols-1 row-cols-sm-2 row-cols-md-3 g-4 mb-4">
        <ArticleCard v-for="article in articles" :key="article.id" :article="article" />
      </div>
      <div v-else class="alert alert-info text-center" role="alert">
        Chưa có bài viết nào được đăng.
      </div>

      <BasePagination
        v-if="totalPages > 1"
        :current-page="currentPage"
        :total-pages="totalPages"
        @page-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getArticles } from "@/http/modules/public/articleService.js";
import ArticleCard from "@/components/article/ArticleCard.vue";
import BasePagination from "@/components/common/BasePagination.vue";

const route = useRoute();
const router = useRouter();

const articles = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const itemsPerPage = ref(9);

const fetchArticles = async (page = 0) => {
  loading.value = true;
  error.value = null;
  articles.value = [];
  try {
    const params = {
      page: page,
      size: itemsPerPage.value,
      sort: "publishedAt,desc",
    };
    const response = await getArticles(params);
    articles.value = response.data.content || [];
    currentPage.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch (err) {
    console.error("Error fetching articles:", err);
    error.value = "Không thể tải danh sách bài viết.";
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (newPage) => {
  router.push({ query: { ...route.query, page: newPage } });
};

watch(
  () => route.query.page,
  (newPageQuery) => {
    const pageNum = parseInt(newPageQuery, 10) || 0;
    fetchArticles(pageNum);
  },
  { immediate: true }
);
</script>

<style scoped>
.article-list-view {
  min-height: 60vh;
}
</style>
