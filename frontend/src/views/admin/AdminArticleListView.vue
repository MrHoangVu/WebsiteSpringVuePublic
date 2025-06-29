<template>
  <div class="admin-article-list-view container mt-4 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1>Quản lý Bài viết</h1>
      <router-link :to="{ name: 'adminArticleNew' }" class="btn btn-primary">
        <i class="bi bi-plus-lg"></i> Thêm Bài viết mới
      </router-link>
    </div>

    <!-- Filters -->
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <form @submit.prevent="applyTableFilters">
          <div class="row g-3 align-items-end">
            <div class="col-md-5">
              <label for="filterKeyword" class="form-label">Tìm kiếm</label>
              <input type="text" class="form-control form-control-sm" id="filterKeyword"
                     v-model="filters.keyword" placeholder="Tiêu đề, nội dung, tóm tắt...">
            </div>
            <div class="col-md-4">
              <label for="filterStatus" class="form-label">Trạng thái</label>
              <select class="form-select form-select-sm" id="filterStatus"
                      v-model="filters.isPublished">
                <option :value="null">-- Tất cả --</option>
                <option :value="true">Đã xuất bản</option>
                <option :value="false">Bản nháp</option>
              </select>
            </div>
            <div class="col-md-3 d-flex gap-2">
              <button type="submit" class="btn btn-primary btn-sm flex-grow-1">
                <i class="bi bi-funnel"></i> Lọc
              </button>
              <button type="button" class="btn btn-outline-secondary btn-sm flex-grow-1"
                      @click="resetFilters" title="Xóa bộ lọc">
                <i class="bi bi-x-lg"></i> Xóa lọc
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center my-5 py-5">
      <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
        <span class="visually-hidden">Đang tải bài viết...</span>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger">
      Lỗi tải danh sách bài viết: {{ error }}
    </div>

    <!-- Article Table -->
    <div v-else-if="articles.length > 0" class="table-responsive card shadow-sm">
      <table class="table table-hover table-striped mb-0 align-middle">
        <thead class="table-light">
        <tr>
          <th scope="col">Tiêu đề</th>
          <th scope="col">Tác giả</th>
          <th scope="col">Ngày tạo</th>
          <th scope="col">Ngày xuất bản</th>
          <th scope="col" class="text-center">Trạng thái</th>
          <th scope="col" class="text-center" style="width: 150px;">Hành Động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="article in articles" :key="article.id">
          <td class="fw-medium">
            <router-link :to="{ name: 'adminArticleEdit', params: { id: article.id } }"
                         class="link-dark text-decoration-none" :title="article.title">
              {{ truncateText(article.title, 60) }} <!-- Rút gọn tiêu đề dài -->
            </router-link>
            <small v-if="article.slug" class="d-block text-muted">Slug: {{ article.slug }}</small>
          </td>
          <td>{{ article.authorUsername || 'N/A' }}</td>
          <td>{{ formatDate(article.createdAt) }}</td>
          <td>{{ article.publishedAt ? formatDate(article.publishedAt) : '-' }}</td>
          <td class="text-center">
              <span class="badge rounded-pill"
                    :class="article.isPublished ? 'text-bg-success' : 'text-bg-secondary'">
                {{ article.isPublished ? 'Xuất bản' : 'Nháp' }}
              </span>
          </td>
          <td class="text-center action-buttons">
            <!-- Edit Button -->
            <router-link :to="{ name: 'adminArticleEdit', params: { id: article.id } }"
                         class="btn btn-sm btn-outline-secondary me-1" title="Sửa bài viết">
              <i class="bi bi-pencil-square"></i>
            </router-link>
            <!-- Publish/Unpublish Button -->
            <button
              v-if="article.isPublished"
              class="btn btn-sm btn-outline-warning me-1"
              title="Chuyển thành nháp"
              @click="confirmTogglePublish(article, false)"
              :disabled="togglingPublishId === article.id">
              <span v-if="togglingPublishId === article.id && !isPublishingAction"
                    class="spinner-border spinner-border-sm" aria-hidden="true"></span>
              <i v-else class="bi bi-arrow-down-square"></i>
            </button>
            <button
              v-else
              class="btn btn-sm btn-outline-success me-1"
              title="Xuất bản bài viết"
              @click="confirmTogglePublish(article, true)"
              :disabled="togglingPublishId === article.id">
              <span v-if="togglingPublishId === article.id && isPublishingAction"
                    class="spinner-border spinner-border-sm" aria-hidden="true"></span>
              <i v-else class="bi bi-arrow-up-square"></i>
            </button>
            <!-- Delete Button -->
            <button
              class="btn btn-sm btn-outline-danger"
              title="Xóa bài viết"
              @click="confirmDelete(article)"
              :disabled="deletingId === article.id">
              <span v-if="deletingId === article.id" class="spinner-border spinner-border-sm"
                    aria-hidden="true"></span>
              <i v-else class="bi bi-trash"></i>
            </button>
          </td>
        </tr>
        </tbody>
      </table>
      <!-- Pagination -->
      <div class="card-footer bg-light border-top-0" v-if="totalPages > 1">
        <BasePagination
          :current-page="currentPage"
          :total-pages="totalPages"
          @page-change="handlePageChange"
          class="mt-3 d-flex justify-content-center mb-0"
        />
      </div>
    </div>

    <!-- No Articles State -->
    <div v-else class="alert alert-info text-center py-5">
      <i class="bi bi-journal-x fs-1 d-block mb-3 text-secondary"></i>
      <h4>Không tìm thấy bài viết nào.</h4>
      <p class="text-muted" v-if="filters.keyword || filters.isPublished !== null">
        Hãy thử xóa bộ lọc hoặc tìm kiếm với từ khóa khác.
      </p>
      <router-link v-else :to="{ name: 'adminArticleNew' }" class="btn btn-link p-0 ms-1">Tạo bài
        viết mới
      </router-link>
      .
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, watch} from 'vue';
import {useRouter, useRoute, RouterLink} from 'vue-router';
import {
  getAdminArticles,
  deleteAdminArticle,
  publishAdminArticle,
  unpublishAdminArticle
} from '@/http/modules/admin/adminArticleService.js';
import BasePagination from '@/components/common/BasePagination.vue';
import {formatDate} from '@/utils/formatters'; // Chỉ cần formatDate

const router = useRouter();
const route = useRoute();

// Component State
const articles = ref([]);
const loading = ref(true);
const error = ref(null);
const currentPage = ref(0);
const totalPages = ref(0);
const itemsPerPage = ref(15); // Số bài viết mỗi trang

// Filter State
const filters = reactive({
  keyword: route.query.keyword || '',
  isPublished: route.query.isPublished !== undefined ? (route.query.isPublished === 'true') : null,
});

// Action States
const togglingPublishId = ref(null);
const isPublishingAction = ref(false); // True if publishing, false if unpublishing
const deletingId = ref(null);

// Fetch Articles Function
const fetchArticles = async (page = 0) => {
  loading.value = true;
  error.value = null;
  try {
    const params = {
      page,
      size: itemsPerPage.value,
      sort: route.query.sort || 'createdAt,desc', // Lấy sort từ URL hoặc mặc định
      ...(filters.keyword && {keyword: filters.keyword}),
      // Chỉ gửi isPublished nếu nó không phải là null
      ...(filters.isPublished !== null && {isPublished: filters.isPublished}),
    };
    const response = await getAdminArticles(params);
    articles.value = response.data.content || [];
    currentPage.value = response.data.number;
    totalPages.value = response.data.totalPages;
  } catch (err) {
    console.error("Error fetching admin articles:", err);
    error.value = "Không thể tải danh sách bài viết.";
    articles.value = []; // Reset khi lỗi
    totalPages.value = 0;
  } finally {
    loading.value = false;
  }
};

// Filter Handling
const applyTableFilters = () => {
  const query = {...route.query, page: 0}; // Reset về trang 0
  if (filters.keyword) query.keyword = filters.keyword; else delete query.keyword;
  if (filters.isPublished !== null) query.isPublished = filters.isPublished; else delete query.isPublished;
  router.push({query});
};

const resetFilters = () => {
  filters.keyword = '';
  filters.isPublished = null;
  const query = {...route.query};
  delete query.keyword;
  delete query.isPublished;
  query.page = 0;
  router.push({query});
};

// Pagination Handling
const handlePageChange = (newPage) => {
  router.push({query: {...route.query, page: newPage}});
};

// Action Handlers
const confirmTogglePublish = async (article, publishAction) => {
  if (togglingPublishId.value) return;
  const actionText = publishAction ? 'xuất bản' : 'chuyển thành bản nháp';
  if (!confirm(`Bạn có chắc muốn ${actionText} bài viết "${article.title}"?`)) return;

  togglingPublishId.value = article.id;
  isPublishingAction.value = publishAction;
  try {
    const apiCall = publishAction ? publishAdminArticle : unpublishAdminArticle;
    const response = await apiCall(article.id);
    const updatedArticle = response.data;
    const index = articles.value.findIndex(a => a.id === updatedArticle.id);
    if (index !== -1) {
      articles.value[index] = updatedArticle;
    } else {
      fetchArticles(currentPage.value);
    }
    // TODO: Show success toast
  } catch (err) {
    console.error(`Error ${actionText} article ${article.id}:`, err);
    alert(`Lỗi khi ${actionText} bài viết: ${err.response?.data?.message || err.message}`);
    // TODO: Show error toast
  } finally {
    togglingPublishId.value = null;
  }
};

const confirmDelete = async (article) => {
  if (deletingId.value) return;
  if (!confirm(`Bạn có chắc muốn XÓA VĨNH VIỄN bài viết "${article.title}"? Hành động này không thể hoàn tác.`)) return;

  deletingId.value = article.id;
  try {
    await deleteAdminArticle(article.id);
    articles.value = articles.value.filter(a => a.id !== article.id);
    if (articles.value.length === 0 && currentPage.value > 0) {
      handlePageChange(currentPage.value - 1);
    }
    // TODO: Show success toast
  } catch (err) {
    console.error(`Error deleting article ${article.id}:`, err);
    alert(`Lỗi khi xóa bài viết: ${err.response?.data?.message || err.message}`);
    // TODO: Show error toast
  } finally {
    deletingId.value = null;
  }
};

// Helper
const truncateText = (text, length) => {
  if (!text) return '';
  if (text.length <= length) return text;
  return text.substring(0, length) + '...';
};


// Watcher for route query changes
watch(
  () => route.query,
  (newQuery) => {
    filters.keyword = newQuery.keyword || '';
    filters.isPublished = newQuery.isPublished !== undefined ? (newQuery.isPublished === 'true') : null;
    fetchArticles(parseInt(newQuery.page || '0', 10));
  },
  {immediate: true, deep: true}
);

</script>

<style scoped>
.admin-article-list-view {
  min-height: 80vh;
}

.table th, .table td {
  vertical-align: middle;
}

.action-buttons .btn {
  min-width: 38px;
}

/* Ensure consistent button width */
.link-dark {
  transition: color 0.2s ease;
}

.link-dark:hover {
  color: var(--bs-primary) !important;
}
</style>
