<template>
  <div class="create-article-view container mt-4 mb-5">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <h1 class="text-center mb-4">Tạo Bài Viết Mới</h1>
        <div class="card shadow-sm">
          <div class="card-body p-4">
            <form @submit.prevent="handleSubmit">
              <!-- Title -->
              <div class="mb-3">
                <label for="articleTitle" class="form-label"
                  >Tiêu đề <span class="text-danger">*</span></label
                >
                <input
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.title }"
                  id="articleTitle"
                  v-model="formData.title"
                  required
                  :disabled="loading"
                />
                <div v-if="validationErrors.title" class="invalid-feedback">
                  {{ validationErrors.title }}
                </div>
              </div>

              <!-- Content -->
              <div class="mb-3">
                <label for="articleContent" class="form-label"
                  >Nội dung <span class="text-danger">*</span></label
                >
                <!-- Nên dùng một Rich Text Editor ở đây thay vì textarea đơn giản -->
                <textarea
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.content }"
                  id="articleContent"
                  rows="10"
                  v-model="formData.content"
                  required
                  :disabled="loading"
                ></textarea>
                <small class="form-text text-muted">Nội dung cần ít nhất 50 ký tự.</small>
                <div v-if="validationErrors.content" class="invalid-feedback">
                  {{ validationErrors.content }}
                </div>
              </div>

              <!-- Excerpt -->
              <div class="mb-3">
                <label for="articleExcerpt" class="form-label">Tóm tắt (tùy chọn)</label>
                <textarea
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.excerpt }"
                  id="articleExcerpt"
                  rows="3"
                  v-model="formData.excerpt"
                  :disabled="loading"
                ></textarea>
                <div v-if="validationErrors.excerpt" class="invalid-feedback">
                  {{ validationErrors.excerpt }}
                </div>
              </div>

              <!-- Featured Image URL -->
              <div class="mb-3">
                <label for="articleImageUrl" class="form-label">URL Ảnh đại diện (tùy chọn)</label>
                <input
                  type="url"
                  class="form-control"
                  :class="{ 'is-invalid': validationErrors.featuredImageUrl }"
                  id="articleImageUrl"
                  v-model="formData.featuredImageUrl"
                  placeholder="https://example.com/image.jpg"
                  :disabled="loading"
                />
                <div v-if="validationErrors.featuredImageUrl" class="invalid-feedback">
                  {{ validationErrors.featuredImageUrl }}
                </div>
              </div>

              <!-- Submit Button & Messages -->
              <div v-if="error" class="alert alert-danger">{{ error }}</div>
              <div v-if="successMessage" class="alert alert-success">
                {{ successMessage }}
                <router-link
                  v-if="createdArticleSlug"
                  :to="{ name: 'articleDetail', params: { slug: createdArticleSlug } }"
                  >Xem bài viết</router-link
                >
              </div>

              <div class="d-grid">
                <button
                  type="submit"
                  class="btn btn-primary btn-lg"
                  :disabled="loading || !!successMessage"
                >
                  <span
                    v-if="loading"
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  {{ loading ? "Đang gửi..." : "Gửi Bài Viết (Chờ duyệt)" }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter, RouterLink } from "vue-router"; // Import RouterLink
import { createArticle } from "@/http/modules/public/articleService.js";
// import { useAuthStore } from '@/store/auth'; // Không cần store trực tiếp vì router guard đã kiểm tra

const router = useRouter();
const formData = reactive({
  title: "",
  content: "",
  excerpt: "",
  featuredImageUrl: "",
});
const validationErrors = reactive({});
const loading = ref(false);
const error = ref(null);
const successMessage = ref(null);
const createdArticleSlug = ref(null); // Lưu slug bài viết vừa tạo

// Client-side validation (cơ bản)
const validateForm = () => {
  Object.keys(validationErrors).forEach((key) => delete validationErrors[key]); // Clear lỗi cũ
  let isValid = true;
  if (!formData.title || formData.title.length < 5 || formData.title.length > 250) {
    validationErrors.title = "Tiêu đề phải từ 5 đến 250 ký tự.";
    isValid = false;
  }
  if (!formData.content || formData.content.length < 50) {
    validationErrors.content = "Nội dung phải có ít nhất 50 ký tự.";
    isValid = false;
  }
  if (formData.excerpt && formData.excerpt.length > 500) {
    validationErrors.excerpt = "Tóm tắt không quá 500 ký tự.";
    isValid = false;
  }
  if (formData.featuredImageUrl) {
    try {
      new URL(formData.featuredImageUrl);
    } catch (_) {
      // Check if valid URL format
      validationErrors.featuredImageUrl = "URL ảnh không hợp lệ.";
      isValid = false;
    }
  }
  return isValid;
};

const handleSubmit = async () => {
  error.value = null;
  successMessage.value = null;
  createdArticleSlug.value = null;

  if (!validateForm()) return;

  loading.value = true;
  try {
    const response = await createArticle(formData);
    successMessage.value = "Bài viết của bạn đã được gửi thành công và đang chờ duyệt.";
    createdArticleSlug.value = response.data.slug; // Lưu slug để tạo link xem trước (nếu cần)
    // Reset form
    Object.keys(formData).forEach((key) => (formData[key] = ""));
    // Có thể chuyển hướng sau vài giây
    // setTimeout(() => router.push({ name: 'articleList' }), 3000);
  } catch (err) {
    console.error("Error creating article:", err);
    if (err.response && err.response.data && err.response.data.message) {
      error.value = err.response.data.message;
    } else if (err.response && err.response.status === 400) {
      // Validation error từ backend
      error.value = "Dữ liệu gửi lên không hợp lệ. Vui lòng kiểm tra lại.";
      // Có thể xử lý chi tiết lỗi validation từ backend nếu cần
    } else {
      error.value = "Đã có lỗi xảy ra khi gửi bài viết.";
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.create-article-view {
  min-height: 70vh;
}
.is-invalid {
  border-color: var(--bs-danger);
}
.invalid-feedback {
  display: block;
  color: var(--bs-danger);
  font-size: 0.875em;
}
</style>
