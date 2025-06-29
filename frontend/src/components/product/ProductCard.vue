<template>
  <div class="col">
    <div class="card h-100 product-card shadow-sm">
      <router-link
        :to="{ name: 'productDetail', params: { slug: product.slug } }"
        class="text-decoration-none text-dark product-link"
      >
        <div class="product-image-container">
          <img
            :src="product.imageUrl || defaultImage"
            class="card-img-top product-image"
            :alt="product.name"
            @error="onImageError"
            loading="lazy"
          />

          <button
            v-if="product.stock > 0 && !authStore.isAdmin"
            type="button"
            class="btn btn-add-to-cart"
            title="Thêm vào giỏ hàng"
            aria-label="Thêm vào giỏ hàng"
            @click.stop.prevent="handleAddToCart"
            :disabled="isAdding"
          >
            <span
              v-if="isAdding"
              class="spinner-border spinner-border-sm"
              role="status"
              aria-hidden="true"
            ></span>
            <img
              v-else
              src="https://www.iconpacks.net/icons/2/free-add-to-cart-icon-3046-thumb.png"
              alt="Thêm vào giỏ"
              class="cart-icon-img"
            />
          </button>

          <div v-else-if="product.stock <= 0" class="out-of-stock-overlay">Hết hàng</div>
        </div>
        <div class="card-body d-flex flex-column">
          <h5 class="card-title product-name flex-grow-1">{{ product.name }}</h5>
          <p class="card-text product-price fw-bold">{{ formatCurrency(product.price) }}</p>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref } from "vue";
import { RouterLink, useRouter, useRoute } from "vue-router";
import defaultImage from "@/assets/images/placeholder.png";
import { useCartStore } from "@/store/cart";
import { useAuthStore } from "@/store/auth";

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const cartStore = useCartStore();
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const isAdding = ref(false);

const formatCurrency = (value) => {
  if (value === null || value === undefined || typeof value !== "number") return "";
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
};

const onImageError = (event) => {
  event.target.src = defaultImage;
};

const handleAddToCart = async () => {
  if (!authStore.isAuthenticated) {
    authStore.setReturnUrl(route.fullPath);
    router.push({ name: "login" });
    return;
  }

  if (authStore.isAdmin || !props.product || props.product.stock <= 0 || isAdding.value) {
    return;
  }

  isAdding.value = true;
  try {
    await cartStore.addItem(props.product.id, 1);
    // Optional: Show success toast
  } catch (error) {
    console.error(`ProductCard: Error adding product ${props.product.id} to cart:`, error);
    // Optional: Show error toast
  } finally {
    setTimeout(() => {
      isAdding.value = false;
    }, 300);
  }
};
</script>

<style scoped>
.product-card {
  /* Sử dụng biến CSS từ theme.css */
  border: var(--bs-border-width, 1px) solid var(--card-border);
  border-radius: var(--bs-border-radius-lg, 0.5rem);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background-color: var(--card-bg);
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--bs-box-shadow-lg); /* Sử dụng biến Bootstrap */
}

.product-link {
  display: block;
  color: inherit;
  text-decoration: none;
}
.product-link:hover {
  color: inherit;
}

.product-image-container {
  position: relative;
  overflow: hidden;
}

.product-image {
  height: 250px;
  width: 100%;
  object-fit: cover;
  /* Sử dụng biến CSS từ theme.css */
  border-bottom: var(--bs-border-width, 1px) solid var(--card-border);
  transition: transform 0.35s ease;
  display: block;
}
.product-card:hover .product-image {
  transform: scale(1.05);
}

.card-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.product-name {
  font-size: 1rem;
  font-weight: 600;
  /* Sử dụng biến CSS từ theme.css */
  color: var(--text-primary);
  margin-bottom: 0.5rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 2.8em;
  flex-grow: 1;
  transition: color 0.2s ease;
}
.product-link:hover .product-name {
  /* Sử dụng biến CSS từ theme.css */
  color: var(--bs-primary);
}

.product-price {
  /* Sử dụng biến CSS từ theme.css */
  color: var(--bs-danger);
  font-size: 1.15rem;
  font-weight: 700;
  margin-top: auto;
  margin-bottom: 0;
}

/* Nút Thêm vào giỏ hàng */
.btn-add-to-cart {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%) translateY(15px);
  opacity: 0;
  visibility: hidden;
  /* Sử dụng biến CSS từ theme.css */
  background-color: var(--accent-color);
  color: var(--bs-dark); /* Sẽ đổi thành màu sáng ở dark mode */
  border: none;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--bs-box-shadow); /* Sử dụng biến Bootstrap */
  transition: opacity 0.3s ease, transform 0.3s ease, background-color 0.2s ease, visibility 0.3s ease;
  z-index: 10;
  cursor: pointer;
}

.product-card:hover .btn-add-to-cart {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
  visibility: visible;
}

.btn-add-to-cart:hover:not(:disabled) {
  /* Sử dụng biến CSS từ theme.css */
  background-color: var(--accent-color-hover);
  box-shadow: var(--bs-box-shadow-lg); /* Sử dụng biến Bootstrap */
}

.btn-add-to-cart:disabled {
  /* Sử dụng biến CSS từ theme.css */
  background-color: var(--text-secondary); /* Màu xám mờ khi disable */
  cursor: not-allowed;
  opacity: 0.7 !important;
  transform: translateX(-50%) translateY(0);
}

.btn-add-to-cart .cart-icon-img {
  width: 24px;
  height: 24px;
  vertical-align: middle;
}

.btn-add-to-cart .spinner-border-sm {
  width: 1.2rem;
  height: 1.2rem;
  color: inherit;
}

/* Thông báo Hết hàng */
.out-of-stock-overlay {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  /* Nền và màu chữ tự điều chỉnh theo theme */
  background-color: rgba(var(--bs-dark-rgb), 0.7); /* Đen mờ (sáng) / Trắng mờ (tối) */
  color: var(--bs-dark); /* Trắng (sáng) / Đen (tối) */
  padding: 5px 12px;
  border-radius: var(--bs-border-radius-sm, 0.25rem); /* Sử dụng biến Bootstrap */
  font-size: 0.8rem;
  font-weight: 500;
  text-align: center;
  z-index: 11;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.product-card:hover .out-of-stock-overlay {
  opacity: 1;
  visibility: visible;
}
</style>
