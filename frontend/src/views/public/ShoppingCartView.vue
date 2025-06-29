<template>
  <div class="shopping-cart-view container mt-4 mb-5">
    <h1 class="text-center mb-4">Giỏ Hàng Của Bạn</h1>

    <!-- Loading State -->
    <div
      v-if="cartStore.isLoading && !cartStore.cartId && cartStore.items.length === 0"
      class="text-center my-5 py-5"
    >
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem" role="status">
        <span class="visually-hidden">Đang tải giỏ hàng...</span>
      </div>
      <p class="mt-3 text-muted">Đang tải giỏ hàng của bạn...</p>
    </div>

    <!-- Error State -->
    <div
      v-else-if="cartStore.error && !cartStore.cartId && cartStore.items.length === 0"
      class="alert alert-warning text-center py-4"
    >
      <i class="bi bi-exclamation-triangle fs-2 d-block mb-2"></i>
      <p class="mb-1">Không thể tải giỏ hàng:</p>
      <p class="mb-0 small">{{ cartStore.error }}</p>
      <button @click="retryFetchCart" class="btn btn-sm btn-outline-secondary mt-2">Thử lại</button>
    </div>

    <!-- Empty Cart State -->
    <div
      v-else-if="!cartStore.isLoading && cartStore.items.length === 0"
      class="alert alert-info text-center py-5"
    >
      <i class="bi bi-cart-x fs-1 d-block mb-3 text-secondary"></i>
      <h4>Giỏ hàng của bạn hiện đang trống.</h4>
      <router-link :to="{ name: 'productList' }" class="btn btn-outline-primary mt-3">
        <i class="bi bi-arrow-left"></i> Tiếp tục mua sắm
      </router-link>
    </div>

    <!-- Cart Content -->
    <div v-else>
      <!-- Error Message During Update/Remove -->
      <div v-if="cartStore.error && cartStore.cartId" class="alert alert-danger small p-2 mb-3">
        <i class="bi bi-exclamation-circle me-1"></i> {{ cartStore.error }}
      </div>

      <div class="row g-4">
        <!-- Product List Column -->
        <div class="col-lg-8 mb-4 mb-lg-0">
          <div class="card shadow-sm cart-items-card">
            <!-- Loại bỏ bg-light -->
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">Sản phẩm trong giỏ ({{ cartStore.totalItemsCount }} sản phẩm)</h5>
              <button
                class="btn btn-outline-danger btn-sm"
                @click="clearCart"
                :disabled="cartStore.isLoading && removingItemId === 'all'"
              >
                <span
                  v-if="cartStore.isLoading && removingItemId === 'all'"
                  class="spinner-border spinner-border-sm me-1"
                  role="status"
                  aria-hidden="true"
                ></span>
                <i v-else class="bi bi-x-lg"></i> Xóa hết
              </button>
            </div>
            <div class="card-body p-0">
              <ul class="list-group list-group-flush">
                <li
                  v-for="item in cartStore.items"
                  :key="item.productId"
                  class="list-group-item px-3 py-3"
                >
                  <div class="row align-items-center">
                    <!-- Image -->
                    <div class="col-3 col-md-2 col-lg-1">
                      <img
                        :src="item.imageUrl || defaultImage"
                        :alt="item.productName"
                        class="img-fluid rounded cart-item-image"
                        @error="onImageError"
                      />
                    </div>
                    <!-- Name, Price, Mobile Remove Button -->
                    <div class="col-9 col-md-5 col-lg-6">
                      <!-- Thay text-dark bằng text-primary -->
                      <router-link
                        :to="{
                          name: 'productDetail',
                          params: { slug: item.slug || item.productId },
                        }"
                        class="text-decoration-none text-primary fw-medium mb-1 d-block line-clamp-2 product-name-link"
                      >{{ item.productName }}</router-link
                      >
                      <small class="text-muted d-block"
                      >Đơn giá: {{ formatCurrency(item.price) }}</small
                      >
                      <button
                        class="btn btn-link text-danger btn-sm p-0 mt-1 d-md-none"
                        @click="removeFromCart(item.productId)"
                        :disabled="removingItemId === item.productId || cartStore.isLoading"
                      >
                        <span
                          v-if="removingItemId === item.productId"
                          class="spinner-border spinner-border-sm"
                        ></span>
                        <i v-else class="bi bi-trash"></i> Xóa
                      </button>
                    </div>
                    <!-- Quantity -->
                    <div class="col-6 col-md-3 col-lg-2 mt-2 mt-md-0">
                      <div class="input-group input-group-sm quantity-control">
                        <button
                          class="btn btn-outline-secondary px-2"
                          type="button"
                          @click="updateQuantity(item.productId, item.quantity - 1)"
                          :disabled="
                            item.quantity <= 1 ||
                            updatingItemId === item.productId ||
                            cartStore.isLoading
                          "
                        >
                          -
                        </button>
                        <input
                          type="number"
                          class="form-control text-center px-1 no-spinners"
                          :value="item.quantity"
                          @change="handleQuantityChange($event, item.productId)"
                          min="1"
                          :disabled="updatingItemId === item.productId || cartStore.isLoading"
                          aria-label="Số lượng"
                        />
                        <button
                          class="btn btn-outline-secondary px-2"
                          type="button"
                          @click="updateQuantity(item.productId, item.quantity + 1)"
                          :disabled="updatingItemId === item.productId || cartStore.isLoading"
                        >
                          +
                        </button>
                        <span
                          v-if="updatingItemId === item.productId"
                          class="spinner-border spinner-border-sm ms-1 align-self-center"
                          role="status"
                          aria-hidden="true"
                        ></span>
                      </div>
                    </div>
                    <!-- Subtotal & Desktop Remove Button -->
                    <div class="col-6 col-md-2 col-lg-3 text-end mt-2 mt-md-0">
                      <span class="fw-bold d-block mb-1">{{
                          formatCurrency(item.price * item.quantity)
                        }}</span>
                      <button
                        class="btn btn-link text-danger btn-sm p-0 d-none d-md-inline-block"
                        @click="removeFromCart(item.productId)"
                        :disabled="removingItemId === item.productId || cartStore.isLoading"
                        title="Xóa sản phẩm"
                      >
                        <span
                          v-if="removingItemId === item.productId"
                          class="spinner-border spinner-border-sm"
                        ></span>
                        <i v-else class="bi bi-trash"></i>
                      </button>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Summary & Promotion Column -->
        <div class="col-lg-4">
          <div class="card shadow-sm sticky-top summary-card" style="top: 80px">
            <!-- Loại bỏ bg-light -->
            <div class="card-header"><h5 class="mb-0">Tóm Tắt Đơn Hàng</h5></div>
            <div class="card-body">
              <!-- Promotion Code -->
              <div class="mb-3">
                <label for="promotionCode" class="form-label">Mã khuyến mãi</label>
                <div class="input-group">
                  <input
                    type="text"
                    class="form-control"
                    id="promotionCode"
                    placeholder="Nhập mã"
                    v-model.trim="promotionCodeInput"
                    :disabled="applyingPromotion || !!appliedPromotion || cartStore.isLoading"
                  />
                  <button
                    class="btn btn-secondary"
                    type="button"
                    @click="handleApplyPromotion"
                    :disabled="
                      applyingPromotion ||
                      !promotionCodeInput ||
                      !!appliedPromotion ||
                      cartStore.isLoading
                    "
                  >
                    <span
                      v-if="applyingPromotion"
                      class="spinner-border spinner-border-sm me-1"
                      role="status"
                      aria-hidden="true"
                    ></span>
                    {{ applyingPromotion ? "Đang..." : "Áp dụng" }}
                  </button>
                </div>
                <div v-if="promotionError" class="text-danger small mt-2">{{ promotionError }}</div>
                <div
                  v-if="appliedPromotion && appliedPromotion.success"
                  class="text-success small mt-2"
                >
                  <i class="bi bi-check-circle-fill"></i> {{ appliedPromotion.message }}
                  <button
                    @click="removePromotion"
                    class="btn btn-link btn-sm text-danger p-0 ms-1"
                    :disabled="cartStore.isLoading"
                  >
                    (Gỡ bỏ)
                  </button>
                </div>
              </div>
              <hr />
              <!-- Total Details -->
              <ul class="list-group list-group-flush mb-3">
                <li class="list-group-item d-flex justify-content-between align-items-center px-0">
                  <span>Tạm tính:</span>
                  <span>{{ formatCurrency(cartStore.subtotal) }}</span>
                </li>
                <li
                  v-if="appliedPromotion && appliedPromotion.success && discountAmount > 0"
                  class="list-group-item d-flex justify-content-between align-items-center px-0 text-success"
                >
                  <span>Giảm giá ({{ appliedPromotion.appliedCode }}):</span>
                  <span>- {{ formatCurrency(discountAmount) }}</span>
                </li>
                <li
                  class="list-group-item d-flex justify-content-between align-items-center px-0 fw-bold fs-5 border-top pt-2"
                >
                  <span>Tổng cộng:</span>
                  <span>{{ formatCurrency(finalTotal) }}</span>
                </li>
              </ul>
              <!-- Checkout Button -->
              <div class="d-grid">
                <button
                  class="btn btn-primary btn-lg"
                  @click="goToCheckout"
                  :disabled="cartStore.items.length === 0 || cartStore.isLoading"
                >
                  <i class="bi bi-credit-card me-2"></i> Tiến hành Thanh toán
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script setup remains unchanged
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { useCartStore } from "@/store/cart.js";
import { applyPromotionCode } from "@/http/modules/public/promotionService.js";
import defaultImage from "@/assets/images/placeholder.png";

const router = useRouter();
const cartStore = useCartStore();

const updatingItemId = ref(null);
const removingItemId = ref(null);
const applyingPromotion = ref(false);

const promotionCodeInput = ref("");
const promotionError = ref(null);
const appliedPromotion = ref(null);

const discountAmount = computed(() =>
  appliedPromotion.value && appliedPromotion.value.success
    ? appliedPromotion.value.discountAmount || 0
    : 0
);

const finalTotal = computed(() => {
  const total = cartStore.subtotal - discountAmount.value;
  return total > 0 ? total : 0;
});

const updateQuantity = async (productId, newQuantity) => {
  if (updatingItemId.value || cartStore.isLoading) return;
  if (newQuantity < 1) return;

  updatingItemId.value = productId;
  try {
    await cartStore.updateQuantity(productId, newQuantity);
    removePromotion();
  } catch (error) {
    console.error("ShoppingCartView: Failed to update quantity", error);
  } finally {
    updatingItemId.value = null;
  }
};

const handleQuantityChange = (event, productId) => {
  const newQuantity = parseInt(event.target.value, 10);
  if (!isNaN(newQuantity) && newQuantity >= 1) {
    updateQuantity(productId, newQuantity);
  } else {
    const currentItem = cartStore.items.find((item) => item.productId === productId);
    event.target.value = currentItem ? currentItem.quantity : 1;
  }
};

const removeFromCart = async (productId) => {
  if (removingItemId.value || cartStore.isLoading) return;

  removingItemId.value = productId;
  try {
    await cartStore.removeItem(productId);
    removePromotion();
  } catch (error) {
    console.error("ShoppingCartView: Failed to remove item", error);
  } finally {
    removingItemId.value = null;
  }
};

const clearCart = async () => {
  if (removingItemId.value || cartStore.isLoading) return;
  if (confirm("Bạn có chắc muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?")) {
    removingItemId.value = "all";
    try {
      await cartStore.clearCart();
      removePromotion();
    } catch (error) {
      console.error("ShoppingCartView: Failed to clear cart", error);
    } finally {
      removingItemId.value = null;
    }
  }
};

const handleApplyPromotion = async () => {
  const code = promotionCodeInput.value.trim();
  if (!code) {
    promotionError.value = "Vui lòng nhập mã khuyến mãi.";
    return;
  }
  if (cartStore.items.length === 0 || applyingPromotion.value || cartStore.isLoading) {
    return;
  }

  applyingPromotion.value = true;
  promotionError.value = null;
  appliedPromotion.value = null;

  try {
    const response = await applyPromotionCode(code);

    if (response.data && response.data.success) {
      appliedPromotion.value = {
        success: true,
        message: response.data.message || `Đã áp dụng mã ${code}`,
        discountAmount: response.data.discountAmount || 0,
        appliedCode: response.data.appliedCode || code,
      };
      promotionCodeInput.value = "";
      savePromotionToSession(appliedPromotion.value);
    } else {
      promotionError.value =
        response.data?.message || "Mã khuyến mãi không hợp lệ hoặc không thể áp dụng.";
      removePromotionFromSession();
    }
  } catch (err) {
    console.error("Error applying promotion:", err);
    if (err.response && err.response.data && err.response.data.message) {
      promotionError.value = err.response.data.message;
    } else {
      promotionError.value = "Đã có lỗi xảy ra khi áp dụng mã khuyến mãi. Vui lòng thử lại.";
    }
    removePromotionFromSession();
  } finally {
    applyingPromotion.value = false;
  }
};

const removePromotion = () => {
  appliedPromotion.value = null;
  promotionError.value = null;
  promotionCodeInput.value = "";
  removePromotionFromSession();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || typeof value !== "number") return "";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
};

const onImageError = (event) => {
  event.target.src = defaultImage;
};

const savePromotionToSession = (promoData) => {
  if (promoData && promoData.success) {
    sessionStorage.setItem(
      "appliedPromo",
      JSON.stringify({
        code: promoData.appliedCode,
        discount: promoData.discountAmount,
        message: promoData.message,
      })
    );
  } else {
    removePromotionFromSession();
  }
};

const removePromotionFromSession = () => {
  sessionStorage.removeItem("appliedPromo");
};

const restorePromotionFromSession = () => {
  try {
    const savedPromo = sessionStorage.getItem("appliedPromo");
    if (savedPromo) {
      const promoData = JSON.parse(savedPromo);
      if (promoData.code && typeof promoData.discount === "number") {
        appliedPromotion.value = {
          success: true,
          message: promoData.message || `Đã áp dụng mã ${promoData.code}`,
          discountAmount: promoData.discount,
          appliedCode: promoData.code,
        };
        console.log("Restored applied promo from session:", appliedPromotion.value);
      } else {
        removePromotionFromSession();
      }
    }
  } catch (e) {
    console.error("Could not parse promo from sessionStorage on mount:", e);
    removePromotionFromSession();
  }
};

const goToCheckout = () => {
  if (cartStore.items.length === 0) {
    alert("Giỏ hàng trống, không thể thanh toán.");
    return;
  }
  savePromotionToSession(appliedPromotion.value);
  router.push({ name: "checkout" });
};

const retryFetchCart = () => {
  cartStore.fetchCart().catch((err) => {
    console.error("ShoppingCartView: Retry cart fetch failed", err);
  });
};

onMounted(() => {
  if (!cartStore.cartId && !cartStore.isLoading) {
    console.log("ShoppingCartView: Initial cart fetch triggered on mount.");
    cartStore
      .fetchCart()
      .catch((err) => console.error("ShoppingCartView: Initial cart fetch failed", err));
  }
  restorePromotionFromSession();
});

watch(
  () => cartStore.items.length,
  (newLength) => {
    if (newLength === 0 && appliedPromotion.value) {
      console.log("Cart became empty, removing applied promotion.");
      removePromotion();
    }
  }
);
</script>

<style scoped>
.shopping-cart-view {
  min-height: 70vh;
}

.cart-item-image {
  max-width: 60px;
  height: 60px;
  object-fit: cover;
}

.quantity-control input.no-spinners {
  -moz-appearance: textfield;
}

.quantity-control input.no-spinners::-webkit-outer-spin-button,
.quantity-control input.no-spinners::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.quantity-control input {
  max-width: 50px !important;
  min-width: 40px;
  text-align: center;
  /* Input background/text/border color should be handled by Bootstrap vars */
  background-color: var(--bs-body-bg); /* Đảm bảo input dùng nền body */
  color: var(--bs-body-color); /* Đảm bảo input dùng màu chữ body */
  border-color: var(--bs-border-color); /* Đảm bảo input dùng màu viền chuẩn */
}

.quantity-control .btn {
  padding: 0.25rem 0.5rem;
  line-height: 1;
}

.quantity-control .spinner-border-sm {
  width: 0.8rem;
  height: 0.8rem;
}

/* Card styles */
.cart-items-card,
.summary-card {
  background-color: var(--card-bg);
  border: 1px solid var(--card-border);
  color: var(--text-primary);
}

.card-header {
  background-color: var(--bg-secondary); /* Nền header card */
  border-bottom: 1px solid var(--card-border);
  color: var(--text-primary); /* Màu chữ header card */
}
.card-header h5 {
  color: var(--text-primary); /* Đảm bảo h5 cũng dùng màu chữ chính */
}

.list-group-item {
  background-color: transparent; /* Để nền của card hiển thị qua */
  border-bottom: 1px solid var(--border-color-subtle); /* Viền giữa các item */
  color: var(--text-primary); /* Màu chữ item */
}
.list-group-item:last-child {
  border-bottom: none; /* Bỏ viền item cuối */
}

/* Đảm bảo list-group-flush trong card-body không có viền/nền riêng */
.list-group-flush {
  border-top: none;
  border-bottom: none;
}

/* Link tên sản phẩm */
.product-name-link {
  color: var(--bs-primary); /* Màu primary cho link */
}
.product-name-link:hover {
  color: var(--bs-primary-dark); /* Đậm hơn khi hover (cần định nghĩa --bs-primary-dark nếu chưa có) */
  /* Hoặc dùng color: var(--bs-link-hover-color); nếu Bootstrap 5.3+ */
}

/* Sticky summary card */
@media (min-width: 992px) {
  .sticky-top {
    position: sticky;
    top: 80px;
    z-index: 1019;
  }
}

/* Remove button link */
.btn-link.text-danger {
  text-decoration: none;
  vertical-align: middle;
}
.btn-link.text-danger:hover {
  text-decoration: underline;
}

.btn-link.text-danger .spinner-border-sm {
  width: 0.8rem;
  height: 0.8rem;
  vertical-align: text-bottom;
}

/* Line clamp */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Mobile remove button */
.d-md-none.btn-link {
  line-height: 1;
}

/* Đảm bảo hr có màu phù hợp */
hr {
  border-top-color: var(--border-color);
}

/* Label màu chữ */
.form-label {
  color: var(--text-secondary);
}
</style>
