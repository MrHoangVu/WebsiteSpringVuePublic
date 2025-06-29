// src/views/ProductDetailView.vue

<template>
  <div class="product-detail-view container py-4">
    <!-- Loading State -->
    <div
      v-if="loading && !product"
      class="alert alert-info text-center py-5 fs-5 shadow-sm"
      role="alert"
    >
      <div class="spinner-border spinner-border-sm me-2" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      Đang tải thông tin sản phẩm...
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="alert alert-danger text-center py-5 fs-5 shadow-sm" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> Lỗi tải dữ liệu:
      {{ error.message || "Không thể tải chi tiết sản phẩm." }}
      <p v-if="error.response && error.response.status === 404" class="mt-2 mb-0 small">
        Sản phẩm bạn tìm không tồn tại (404).
        <router-link :to="{ name: 'productList' }">Quay lại danh sách sản phẩm</router-link>
        .
      </p>
      <p v-else class="mt-2 mb-0 small">
        Vui lòng thử tải lại trang hoặc
        <router-link :to="{ name: 'contact' }">liên hệ</router-link>
        với chúng tôi.
      </p>
    </div>

    <!-- Product Found State -->
    <div v-else-if="product" class="product-content">
      <!-- Product Info Row -->
      <div class="row g-4 g-lg-5 mb-5">
        <!-- Product Gallery Column -->
        <div class="col-lg-6 text-center product-gallery">
          <img
            :src="product.imageUrl || defaultImage"
            :alt="`Ảnh sản phẩm ${product.name}`"
            class="img-fluid rounded shadow-sm product-main-image border"
            @error="onImageError"
            loading="lazy"
          />
          <!-- Có thể thêm gallery nâng cao ở đây -->
        </div>

        <!-- Product Info Column -->
        <div class="col-lg-6 product-info">
          <!-- Category -->
          <div v-if="product.category" class="mb-2">
            <router-link
              :to="{
                name: 'productListByCategory',
                params: { categorySlug: product.category.slug },
              }"
              class="badge text-bg-secondary text-decoration-none product-category-link"
            >
              <i class="bi bi-tag me-1"></i> {{ product.category.name }}
            </router-link>
          </div>

          <!-- Name -->
          <h1 class="product-name h2 mb-2">{{ product.name }}</h1>

          <!-- Average Rating Display -->
          <div class="mb-3 product-rating-summary">
            <span
              v-if="product.averageRating > 0"
              class="review-stars me-2"
              :aria-label="`${product.averageRating.toFixed(1)} out of 5 stars`"
            >
              <i
                v-for="star in 5"
                :key="`avg-star-${star}`"
                class="bi text-warning"
                :class="
                  star <= Math.round(product.averageRating)
                    ? 'bi-star-fill'
                    : star - 0.5 <= product.averageRating
                    ? 'bi-star-half'
                    : 'bi-star'
                "
              ></i>
              ({{ product.averageRating.toFixed(1) }})
            </span>
            <span
              v-else-if="!loadingReviews && product.reviewCount === 0"
              class="text-muted small me-2"
              >(Chưa có đánh giá)</span
            >
            <a
              href="#reviews-content"
              @click.prevent="scrollToReviews"
              class="text-muted text-decoration-none small"
              v-if="product.reviewCount > 0"
            >
              ({{ product.reviewCount || 0 }} đánh giá)
            </a>
          </div>

          <!-- Price -->
          <p class="product-price fs-3 fw-bold text-danger mb-4">
            {{ formatCurrency(product.price) }}
          </p>

          <!-- Attributes -->
          <div class="product-attributes bg-light p-3 rounded mb-4 border">
            <h5 class="h6 mb-3">Thông tin chi tiết:</h5>
            <p v-if="product.dimensions" class="mb-2 small">
              <strong><i class="bi bi-rulers me-1"></i> Kích thước:</strong>
              {{ product.dimensions }}
            </p>
            <p v-if="product.material" class="mb-2 small">
              <strong><i class="bi bi-tree me-1"></i> Chất liệu:</strong> {{ product.material }}
            </p>
            <p v-if="product.stock !== null && product.stock !== undefined" class="mb-0 small">
              <strong><i class="bi bi-box-seam me-1"></i> Tình trạng:</strong>
              <span
                :class="product.stock > 0 ? 'text-success fw-semibold' : 'text-warning fw-semibold'"
              >
                {{ product.stock > 0 ? "Còn hàng" : "Hết hàng" }}
              </span>
              <span v-if="product.stock > 0"> ({{ product.stock }} sản phẩm có sẵn)</span>
            </p>
          </div>

          <!-- === Actions (Ẩn hoàn toàn nếu là Admin) === -->
          <div class="product-actions mb-4" v-if="!authStore.isAdmin">
            <!-- Quantity Selector (Chỉ hiện khi còn hàng) -->
            <div
              class="mb-3 quantity-selector d-flex align-items-center"
              style="max-width: 150px"
              v-if="product.stock > 0"
            >
              <label for="quantityInput" class="form-label me-2 mb-0 flex-shrink-0"
                >Số lượng:</label
              >
              <div class="input-group input-group-sm">
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="changeQuantity(-1)"
                  :disabled="selectedQuantity <= 1 || addingToCart"
                >
                  -
                </button>
                <input
                  type="number"
                  id="quantityInput"
                  class="form-control text-center px-1"
                  v-model.number="selectedQuantity"
                  @change="validateQuantity"
                  min="1"
                  :max="product.stock || 1"
                  :disabled="addingToCart"
                  aria-label="Số lượng sản phẩm"
                />
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  @click="changeQuantity(1)"
                  :disabled="selectedQuantity >= (product.stock || 0) || addingToCart"
                >
                  +
                </button>
              </div>
            </div>
            <!-- Thông báo hết hàng thay cho Quantity Selector -->
            <div v-if="product.stock <= 0" class="alert alert-warning p-2 small">
              Sản phẩm tạm hết hàng
            </div>

            <!-- Add to Cart / Zalo Buttons -->
            <div class="d-flex flex-wrap gap-2 mt-2">
              <!-- Nút Thêm vào giỏ (Disable nếu hết hàng hoặc đang xử lý) -->
              <button
                type="button"
                class="btn btn-success btn-lg flex-grow-1"
                :disabled="addingToCart || product.stock <= 0"
                @click="handleAddToCart"
              >
                <span
                  v-if="addingToCart"
                  class="spinner-border spinner-border-sm me-1"
                  role="status"
                  aria-hidden="true"
                ></span>
                <i v-else class="bi bi-cart-plus me-1"></i>
                {{ addingToCart ? "Đang thêm..." : "Thêm vào giỏ" }}
              </button>
              <!-- Nút Zalo luôn hiển thị (trừ khi là admin) -->
              <button
                type="button"
                @click="openZaloChat"
                class="btn btn-primary btn-lg d-inline-flex align-items-center"
              >
                <img src="../../assets/images/zalo-icon.png" alt="Zalo" class="zalo-icon me-2" />
                Đặt hàng Zalo
              </button>
            </div>

            <!-- Thông báo kết quả thêm vào giỏ -->
            <div
              v-if="addToCartMessage"
              class="alert mt-3 p-2 small"
              :class="addToCartError ? 'alert-danger' : 'alert-success'"
            >
              {{ addToCartMessage }}
            </div>
            <!-- Tin nhắn hết hàng cuối cùng (dự phòng, thường sẽ không hiển thị vì nút Add đã disable) -->
            <p
              v-if="product && product.stock <= 0 && !addToCartMessage"
              class="text-danger small mt-2"
            >
              Sản phẩm hiện đang hết hàng.
            </p>
          </div>
          <!-- === Kết thúc Actions === -->

          <!-- SKU -->
          <div v-if="product.sku" class="product-meta text-muted small mb-3">
            SKU: {{ product.sku }}
          </div>
        </div>
      </div>

      <!-- === Description & Reviews Tabs === -->
      <div class="product-details-tabs">
        <ul class="nav nav-tabs mb-0" id="productTab" role="tablist">
          <li class="nav-item" role="presentation">
            <button
              class="nav-link active"
              id="description-tab"
              data-bs-toggle="tab"
              data-bs-target="#description-content"
              type="button"
              role="tab"
              aria-controls="description-content"
              aria-selected="true"
            >
              <i class="bi bi-card-text me-1"></i> Mô tả sản phẩm
            </button>
          </li>
          <li class="nav-item" role="presentation">
            <button
              class="nav-link position-relative"
              id="reviews-tab"
              data-bs-toggle="tab"
              data-bs-target="#reviews-content"
              type="button"
              role="tab"
              aria-controls="reviews-content"
              aria-selected="false"
            >
              <i class="bi bi-chat-square-text me-1"></i> Đánh giá
              <span
                v-if="product.reviewCount > 0"
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-primary"
              >
                {{ product.reviewCount }}
                <span class="visually-hidden">reviews</span>
              </span>
            </button>
          </li>
        </ul>
        <div
          class="tab-content p-3 p-lg-4 border border-top-0 rounded-bottom shadow-sm"
          id="productTabContent"
        >
          <!-- Description Content -->
          <div
            class="tab-pane fade show active"
            id="description-content"
            role="tabpanel"
            aria-labelledby="description-tab"
          >
            <h3 class="h5 mb-3 d-lg-none">Mô tả sản phẩm</h3>
            <div
              v-if="product.description"
              v-html="product.description"
              class="description-content"
            ></div>
            <p v-else class="text-muted fst-italic">Sản phẩm này chưa có mô tả chi tiết.</p>
          </div>

          <!-- Reviews Content -->
          <div
            class="tab-pane fade"
            id="reviews-content"
            role="tabpanel"
            aria-labelledby="reviews-tab"
          >
            <h3 class="h5 mb-3 d-lg-none">Đánh giá sản phẩm</h3>

            <!-- Nút Viết đánh giá (Chỉ hiện khi đã đăng nhập và không phải admin) -->
            <div
              class="mb-3 text-end"
              v-if="authStore.isAuthenticated && !authStore.isAdmin && !showReviewForm"
            >
              <button class="btn btn-outline-primary btn-sm" @click="toggleReviewForm">
                <i class="bi bi-pencil-square me-1"></i> Viết đánh giá
              </button>
            </div>

            <!-- Review Form (Chỉ hiện khi đã đăng nhập và không phải admin) -->
            <div
              v-if="authStore.isAuthenticated && !authStore.isAdmin && showReviewForm"
              class="card mb-4 shadow-sm border bg-light"
            >
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h4 class="card-title h6 mb-0">Viết đánh giá của bạn cho "{{ product.name }}"</h4>
                  <button
                    type="button"
                    class="btn-close small"
                    @click="toggleReviewForm"
                    aria-label="Đóng form đánh giá"
                  ></button>
                </div>
                <form @submit.prevent="submitReview" novalidate>
                  {/* Rating Input */}
                  <div class="mb-3">
                    <label class="form-label d-block mb-1"
                      >Xếp hạng <span class="text-danger">*</span>:</label
                    >
                    <div class="btn-group review-star-group" role="group" aria-label="Rating">
                      <button
                        type="button"
                        v-for="star in 5"
                        :key="star"
                        class="btn btn-lg"
                        :class="
                          newReview.rating && star <= newReview.rating
                            ? 'btn-warning text-white'
                            : 'btn-outline-warning'
                        "
                        @click="setRating(star)"
                        :disabled="submittingReview"
                        :aria-pressed="newReview.rating === star"
                        :title="`${star} sao`"
                      >
                        <i
                          class="bi"
                          :class="
                            newReview.rating && star <= newReview.rating
                              ? 'bi-star-fill'
                              : 'bi-star'
                          "
                        ></i>
                      </button>
                    </div>
                    <div v-if="reviewFormClientError.rating" class="text-danger small mt-1">
                      {{ reviewFormClientError.rating }}
                    </div>
                  </div>
                  {/* Comment Input */}
                  <div class="mb-3">
                    <label for="reviewComment" class="form-label">Bình luận:</label>
                    <textarea
                      class="form-control"
                      :class="{ 'is-invalid': reviewFormClientError.comment }"
                      id="reviewComment"
                      rows="4"
                      v-model="newReview.comment"
                      :disabled="submittingReview"
                      placeholder="Chia sẻ cảm nhận chi tiết của bạn về sản phẩm..."
                      maxlength="1000"
                    ></textarea>
                    <div class="d-flex justify-content-between">
                      <div v-if="reviewFormClientError.comment" class="invalid-feedback">
                        {{ reviewFormClientError.comment }}
                      </div>
                      <small class="text-muted form-text ms-auto"
                        >{{ newReview.comment.length }}/1000</small
                      >
                    </div>
                  </div>
                  {/* API Error/Success Messages */}
                  <div v-if="reviewSubmitError" class="alert alert-danger p-2 small">
                    {{ reviewSubmitError }}
                  </div>
                  <div v-if="reviewSubmitSuccess" class="alert alert-success p-2 small">
                    {{ reviewSubmitSuccess }}
                  </div>
                  {/* Submit Button */}
                  <button
                    type="submit"
                    class="btn btn-primary px-4"
                    :disabled="submittingReview || !newReview.rating"
                  >
                    <span
                      v-if="submittingReview"
                      class="spinner-border spinner-border-sm me-2"
                      role="status"
                      aria-hidden="true"
                    ></span>
                    {{ submittingReview ? "Đang gửi..." : "Gửi đánh giá" }}
                  </button>
                </form>
              </div>
            </div>
            <!-- Login Prompt (Chỉ hiện khi chưa đăng nhập) -->
            <div
              v-else-if="!authStore.isAuthenticated && !loadingReviews"
              class="alert alert-info small p-3 text-center shadow-sm"
            >
              <i class="bi bi-info-circle me-1"></i> Vui lòng
              <router-link
                :to="{ name: 'login', query: { returnUrl: route.fullPath } }"
                class="fw-semibold"
                >đăng nhập
              </router-link>
              để viết đánh giá cho sản phẩm này.
            </div>

            <!-- === Reviews List === -->
            <div class="reviews-list-section mt-4">
              <h4
                class="h6 mb-3 border-bottom pb-2"
                v-if="reviews.length > 0 || loadingReviews || errorReviews"
              >
                Tất cả đánh giá ({{ product.reviewCount || reviews.length }})
              </h4>
              <!-- Loading Reviews State -->
              <div v-if="loadingReviews" class="text-center my-5 text-muted">
                <div class="spinner-border spinner-border-sm text-secondary" role="status">
                  <span class="visually-hidden">Loading reviews...</span>
                </div>
                <span class="ms-2">Đang tải đánh giá...</span>
              </div>
              <!-- Error Loading Reviews State -->
              <div v-else-if="errorReviews" class="alert alert-warning text-center">
                <i class="bi bi-exclamation-circle me-1"></i> {{ errorReviews }}
              </div>
              <!-- Reviews List Display -->
              <div v-else-if="reviews.length > 0">
                <div
                  v-for="review in reviews"
                  :key="review.id"
                  class="review-item mb-4 border-bottom pb-3"
                >
                  <div class="d-flex justify-content-between align-items-start mb-1 flex-wrap">
                    <div class="review-author mb-1">
                      <span class="fw-semibold me-2">{{ review.reviewerName || "Ẩn danh" }}</span>
                      <span
                        class="review-stars text-warning"
                        :aria-label="`${review.rating} out of 5 stars`"
                      >
                        <i
                          v-for="star in 5"
                          :key="`review-${review.id}-star-${star}`"
                          class="bi"
                          :class="star <= review.rating ? 'bi-star-fill' : 'bi-star'"
                        ></i>
                      </span>
                    </div>
                    <small
                      class="text-muted flex-shrink-0 ms-md-2"
                      :title="formatFullDateTime(review.createdAt)"
                      >{{ formatDateRelative(review.createdAt) }}</small
                    >
                  </div>
                  <p v-if="review.comment" class="mb-2 review-comment">{{ review.comment }}</p>
                  <p v-else class="text-muted fst-italic small mb-2">
                    Người dùng này không để lại bình luận.
                  </p>
                  <!-- Nút trả lời (tạm ẩn) -->
                  <!-- <div class="text-end review-actions">...</div> -->
                  <!-- Phần trả lời (tạm ẩn) -->
                </div>
                <BasePagination
                  v-if="reviewsTotalPages > 1"
                  :current-page="reviewsCurrentPage + 1"
                  :total-pages="reviewsTotalPages"
                  @page-change="handleReviewPageChange"
                  class="mt-4 d-flex justify-content-center"
                />
              </div>
              <!-- No Reviews Message -->
              <div
                v-else-if="!loadingReviews && !errorReviews"
                class="text-muted text-center my-4 p-4 bg-light rounded border"
              >
                <i class="bi bi-chat-square-dots me-1"></i> Chưa có đánh giá nào cho sản phẩm này.
                Hãy là người đầu tiên đánh giá!
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Not Found State (Fallback) -->
    <div
      v-else-if="!loading && !product && !error"
      class="alert alert-warning text-center py-5 fs-5 shadow-sm"
      role="alert"
    >
      <i class="bi bi-search me-2"></i> Không tìm thấy thông tin sản phẩm bạn yêu cầu.
      <p class="mt-2 mb-0 small">
        Có thể sản phẩm đã bị xóa hoặc đường dẫn không đúng.
        <router-link :to="{ name: 'productList' }">Xem các sản phẩm khác</router-link>
        .
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { getProductBySlug } from "@/http/modules/public/productService.js"; // Đảm bảo đường dẫn đúng
import defaultImage from "@/assets/images/placeholder.png"; // Đảm bảo đường dẫn đúng
import { useAuthStore } from "@/store/auth.js";
import { useCartStore } from "@/store/cart.js"; // Đã import
import { getProductReviews, createProductReview } from "@/http/modules/public/reviewService.js"; // Đảm bảo đường dẫn đúng
import BasePagination from "@/components/common/BasePagination.vue"; // Đảm bảo đường dẫn đúng

// --- Core Vue & Router ---
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore(); // Đã sử dụng

// --- Product State ---
const product = ref(null);
const loading = ref(true);
const error = ref(null);
const productSlug = computed(() => route.params.slug);

// --- Cart Action State ---
const selectedQuantity = ref(1);
const addingToCart = ref(false);
const addToCartMessage = ref("");
const addToCartError = ref(false);

// --- Review State ---
const reviews = ref([]);
const loadingReviews = ref(false);
const errorReviews = ref(null);
const reviewsCurrentPage = ref(0); // API page number (0-based)
const reviewsTotalPages = ref(0);
const reviewsPerPage = ref(5); // Số đánh giá mỗi trang
const showReviewForm = ref(false);
const newReview = reactive({ rating: null, comment: "" });
const submittingReview = ref(false);
const reviewSubmitError = ref(null);
const reviewSubmitSuccess = ref(null);
const reviewFormClientError = reactive({ rating: null, comment: null });
const replyingTo = ref(null); // ID đánh giá đang trả lời (chưa dùng)

// === Helper Functions ===
const formatCurrency = (value) => {
  /* ... giữ nguyên ... */
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);
};
const formatFullDateTime = (dateString) => {
  /* ... giữ nguyên ... */
  if (!dateString) return "";
  try {
    return new Date(dateString).toLocaleString("vi-VN", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (e) {
    return dateString;
  }
};
const formatDateRelative = (dateString) => {
  /* ... giữ nguyên ... */
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    if (diffInSeconds < 60) return `vài giây trước`;
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours} giờ trước`;
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return `${diffInDays} ngày trước`;
    const diffInWeeks = Math.floor(diffInDays / 7);
    if (diffInWeeks < 5) return `${diffInWeeks} tuần trước`;
    return date.toLocaleDateString("vi-VN", { year: "numeric", month: "numeric", day: "numeric" });
  } catch (e) {
    return dateString;
  }
};
const onImageError = (event) => {
  /* ... giữ nguyên ... */
  console.warn(`Image failed to load: ${event.target.src}. Using placeholder.`);
  event.target.src = defaultImage;
};
const scrollToReviews = () => {
  /* ... giữ nguyên ... */
  const reviewsElement = document.getElementById("reviews-content");
  if (reviewsElement) {
    const reviewsTab = new bootstrap.Tab(document.getElementById("reviews-tab"));
    reviewsTab.show();
    setTimeout(() => {
      reviewsElement.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 150);
  }
};

// === API Call Functions ===
const fetchProductDetail = async () => {
  /* ... giữ nguyên, đảm bảo reset selectedQuantity ... */
  loading.value = true;
  error.value = null;
  product.value = null;
  selectedQuantity.value = 1;
  if (!productSlug.value) {
    error.value = new Error("Slug sản phẩm không hợp lệ.");
    loading.value = false;
    return;
  }
  try {
    const response = await getProductBySlug(productSlug.value);
    if (response?.data) {
      product.value = response.data;
      resetReviewState();
    } else {
      throw new Error("Không nhận được dữ liệu sản phẩm hợp lệ từ API.");
    }
  } catch (err) {
    console.error("Error fetching product detail:", err);
    error.value = err;
    product.value = null;
  } finally {
    loading.value = false;
  }
};
const fetchProductReviews = async (page = 0) => {
  /* ... giữ nguyên ... */
  if (!product.value?.id) return;
  loadingReviews.value = true;
  errorReviews.value = null;
  try {
    const params = { page: page, size: reviewsPerPage.value, sort: "createdAt,desc" };
    const response = await getProductReviews(product.value.id, params);
    if (response?.data && Array.isArray(response.data.content)) {
      reviews.value = response.data.content;
      reviewsCurrentPage.value = response.data.number;
      reviewsTotalPages.value = response.data.totalPages;
      if (response.data.totalElements !== undefined && product.value) {
        product.value.reviewCount = response.data.totalElements;
      }
    } else {
      throw new Error("Dữ liệu đánh giá trả về không đúng định dạng.");
    }
  } catch (err) {
    console.error("Error fetching reviews:", err);
    errorReviews.value = "Không thể tải danh sách đánh giá. Vui lòng thử lại.";
    reviews.value = [];
    reviewsTotalPages.value = 0;
  } finally {
    loadingReviews.value = false;
  }
};

// === Action Handlers ===

// --- Cart Actions ---
const changeQuantity = (amount) => {
  /* ... giữ nguyên ... */
  const currentStock = product.value?.stock ?? 0;
  let newQuantity = selectedQuantity.value + amount;
  if (newQuantity < 1) newQuantity = 1;
  if (currentStock > 0 && newQuantity > currentStock) {
    newQuantity = currentStock;
  } else if (currentStock <= 0) {
    newQuantity = 1;
  }
  selectedQuantity.value = newQuantity;
};
const validateQuantity = () => {
  /* ... giữ nguyên ... */
  const currentStock = product.value?.stock ?? 0;
  if (!Number.isInteger(selectedQuantity.value) || selectedQuantity.value < 1) {
    selectedQuantity.value = 1;
  } else if (currentStock > 0 && selectedQuantity.value > currentStock) {
    selectedQuantity.value = currentStock;
  }
};
const handleAddToCart = async () => {
  /* ... giữ nguyên, đảm bảo KHÔNG REDIRECT ... */
  if (!authStore.isAuthenticated) {
    authStore.setReturnUrl(route.fullPath);
    router.push({ name: "login" });
    return;
  }
  if (
    authStore.isAdmin ||
    !product.value ||
    product.value.stock <= 0 ||
    selectedQuantity.value <= 0 ||
    addingToCart.value
  ) {
    return;
  }
  addingToCart.value = true;
  addToCartMessage.value = "";
  addToCartError.value = false;
  try {
    console.log(
      `DetailView: Adding ${selectedQuantity.value} of product ${product.value.id} to cart...`
    );
    await cartStore.addItem(product.value.id, selectedQuantity.value);
    addToCartMessage.value = `Đã thêm ${selectedQuantity.value} "${product.value.name}" vào giỏ hàng thành công!`;
    setTimeout(() => {
      addToCartMessage.value = "";
    }, 3000);
  } catch (err) {
    console.error("Error adding item to cart from DetailView:", err);
    addToCartMessage.value =
      err.response?.data?.message || err.message || "Thêm vào giỏ hàng thất bại. Vui lòng thử lại.";
    addToCartError.value = true;
  } finally {
    addingToCart.value = false;
  }
};

// --- Review Actions ---
const handleReviewPageChange = (newPage) => {
  /* ... giữ nguyên, newPage từ BasePagination là 1-based nên cần -1 khi gọi API ... */
  fetchProductReviews(newPage - 1);
  const reviewsElement = document.getElementById("reviews-content");
  reviewsElement?.scrollIntoView({ behavior: "smooth", block: "start" });
};
const toggleReviewForm = () => {
  /* ... giữ nguyên ... */
  showReviewForm.value = !showReviewForm.value;
  if (showReviewForm.value) {
    reviewSubmitError.value = null;
    reviewSubmitSuccess.value = null;
    reviewFormClientError.rating = null;
    reviewFormClientError.comment = null;
    nextTick(() => {
      const formElement = document.getElementById("reviewComment");
      formElement?.scrollIntoView({ behavior: "smooth", block: "center" });
    });
  }
};
const setRating = (star) => {
  /* ... giữ nguyên ... */
  newReview.rating = star;
  reviewFormClientError.rating = null;
};
const validateReviewForm = () => {
  /* ... giữ nguyên ... */
  let isValid = true;
  reviewFormClientError.rating = null;
  reviewFormClientError.comment = null;
  if (!newReview.rating || newReview.rating < 1 || newReview.rating > 5) {
    reviewFormClientError.rating = "Vui lòng chọn từ 1 đến 5 sao.";
    isValid = false;
  }
  if (newReview.comment.length > 1000) {
    reviewFormClientError.comment = "Bình luận không được vượt quá 1000 ký tự.";
    isValid = false;
  }
  return isValid;
};
const submitReview = async () => {
  /* ... giữ nguyên ... */
  if (!validateReviewForm()) return;
  if (!product.value?.id) {
    reviewSubmitError.value = "Lỗi: Không tìm thấy ID sản phẩm.";
    return;
  }
  submittingReview.value = true;
  reviewSubmitError.value = null;
  reviewSubmitSuccess.value = null;
  try {
    const payload = { rating: newReview.rating, comment: newReview.comment.trim() };
    await createProductReview(product.value.id, payload);
    reviewSubmitSuccess.value = "Đánh giá của bạn đã được gửi thành công!";
    setTimeout(() => {
      newReview.rating = null;
      newReview.comment = "";
      showReviewForm.value = false;
      reviewSubmitSuccess.value = null;
    }, 2500);
    setTimeout(() => {
      fetchProductReviews(0);
      fetchProductDetail(); /* Fetch lại product để cập nhật avg rating */
    }, 500);
  } catch (err) {
    console.error("Error submitting review:", err);
    if (err.response?.data?.message) {
      reviewSubmitError.value = err.response.data.message;
    } else if (err.response?.status === 401) {
      reviewSubmitError.value = "Phiên đăng nhập hết hạn.";
    } else {
      reviewSubmitError.value = "Lỗi gửi đánh giá.";
    }
  } finally {
    submittingReview.value = false;
  }
};
const handleReplyClick = (reviewId) => {
  /* ... giữ nguyên ... */
  if (!authStore.isAuthenticated) {
    router.push({ name: "login", query: { returnUrl: route.fullPath } });
    return;
  }
  replyingTo.value = replyingTo.value === reviewId ? null : reviewId;
};

// --- Zalo Action ---
const openZaloChat = () => {
  /* ... giữ nguyên ... */
  if (!product.value) return;
  const shopZaloPhone = "0389366619";
  const productName = product.value.name;
  const productUrl = window.location.href;
  let message = `Chào shop, tôi quan tâm đến sản phẩm: ${productName}.\nLink: ${productUrl}`;
  const encodedMessage = encodeURIComponent(message);
  const zaloLink = `https://zalo.me/${shopZaloPhone}?message=${encodedMessage}`;
  window.open(zaloLink, "_blank", "noopener,noreferrer");
};

// --- Reset State ---
const resetReviewState = () => {
  /* ... giữ nguyên, thêm reset cart message ... */
  reviews.value = [];
  loadingReviews.value = false;
  errorReviews.value = null;
  reviewsCurrentPage.value = 0;
  reviewsTotalPages.value = 0;
  showReviewForm.value = false;
  newReview.rating = null;
  newReview.comment = "";
  submittingReview.value = false;
  reviewSubmitError.value = null;
  reviewSubmitSuccess.value = null;
  reviewFormClientError.rating = null;
  reviewFormClientError.comment = null;
  replyingTo.value = null;
  addToCartMessage.value = "";
  addToCartError.value = false;
};

// === Lifecycle Hooks ===
onMounted(async () => {
  /* ... giữ nguyên ... */
  await fetchProductDetail();
  if (product.value?.id) {
    fetchProductReviews();
  }
});
watch(
  () => route.params.slug,
  async (newSlug, oldSlug) => {
    /* ... giữ nguyên ... */
    if (newSlug && newSlug !== oldSlug && route.name === "productDetail") {
      await fetchProductDetail();
      if (product.value?.id) {
        fetchProductReviews();
      }
    }
  }
);
</script>

<style scoped>
/* --- Styles --- */
/* (Giữ nguyên các style đã có) */
/* === General Product Detail Styles === */
.product-gallery .product-main-image {
  max-height: 550px;
  object-fit: contain;
}

.product-info .product-category-link {
  font-size: 0.85em;
}

.product-info .product-name {
  font-weight: 600;
}

.product-rating-summary .review-stars {
  font-size: 1.1em;
}

.product-rating-summary a:hover {
  text-decoration: underline !important;
}

.product-price {
  letter-spacing: -0.5px;
}

.product-attributes {
  font-size: 0.9em;
}

.product-attributes strong {
  min-width: 80px;
  display: inline-block;
  margin-right: 5px;
}

.product-attributes i {
  width: 1.2em;
  text-align: center;
}

.zalo-icon {
  width: 20px;
  height: 20px;
  vertical-align: middle;
}

/* === Description & v-html Content === */
.description-content :deep(p) {
  line-height: 1.7;
  margin-bottom: 1rem;
}

.description-content :deep(h1),
.description-content :deep(h2),
.description-content :deep(h3),
.description-content :deep(h4),
.description-content :deep(h5),
.description-content :deep(h6) {
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  font-weight: 600;
}

.description-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 1rem 0;
  border-radius: 0.375rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.description-content :deep(ul),
.description-content :deep(ol) {
  padding-left: 2rem;
}

.description-content :deep(blockquote) {
  font-style: italic;
  border-left: 4px solid var(--bs-secondary-bg);
  padding-left: 1rem;
  margin-left: 0;
  color: var(--bs-secondary-color);
}

/* === Tabs Styling === */
.product-details-tabs .nav-tabs {
  border-bottom-color: var(--bs-border-color);
}

.product-details-tabs .nav-tabs .nav-link {
  color: var(--bs-secondary-color);
  border-width: 1px;
  border-color: transparent transparent var(--bs-border-color) transparent;
  border-radius: 0;
  padding: 0.75rem 1.25rem;
  font-weight: 500;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out,
    border-color 0.15s ease-in-out;
}

.product-details-tabs .nav-tabs .nav-link:hover:not(.active) {
  border-color: var(--bs-gray-300) var(--bs-gray-300) var(--bs-border-color) var(--bs-gray-300);
  isolation: isolate;
}

.product-details-tabs .nav-tabs .nav-link.active,
.product-details-tabs .nav-tabs .nav-item.show .nav-link {
  color: var(--bs-primary);
  background-color: var(--bs-body-bg);
  border-color: var(--bs-border-color) var(--bs-border-color) var(--bs-body-bg)
    var(--bs-border-color);
  border-top-left-radius: var(--bs-border-radius);
  border-top-right-radius: var(--bs-border-radius);
  font-weight: 600;
}

.product-details-tabs .tab-content {
  background-color: var(--bs-body-bg);
}

/* === Review Form & List Styling === */
.review-star-group .btn {
  padding: 0.3rem 0.6rem;
  font-size: 1.5rem;
  line-height: 1;
  box-shadow: none !important;
}

.review-star-group .btn i {
  vertical-align: middle;
}

.review-star-group .btn.btn-outline-warning {
  border-color: #ffc107;
}

.reviews-list-section .review-item:last-child {
  border-bottom: none !important;
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}

.reviews-list-section .review-stars {
  line-height: 1;
  font-size: 0.9em;
}

.reviews-list-section .review-stars .bi {
  margin-right: 1px;
}

.reviews-list-section .review-comment {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 0.95em;
  color: var(--bs-body-color);
  margin-top: 0.25rem;
}

.reviews-list-section .review-actions {
  margin-top: 0.5rem;
}

.reviews-list-section .review-actions .btn-link {
  font-size: 0.85em;
  padding: 0.25rem 0.5rem;
}

.reviews-list-section .review-actions .btn-link i {
  font-size: 1.1em;
  vertical-align: middle;
  margin-bottom: 2px;
}

.reply-section {
  background-color: var(--bs-tertiary-bg);
  padding: 0.75rem 1rem;
  border-radius: var(--bs-border-radius);
  margin-top: 0.75rem;
  border-left-width: 3px !important;
  border-color: var(--bs-primary-border-subtle) !important;
}

/* === Styles for Quantity Selector === */
.quantity-selector input[type="number"] {
  -moz-appearance: textfield;
}

.quantity-selector input[type="number"]::-webkit-outer-spin-button,
.quantity-selector input[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
