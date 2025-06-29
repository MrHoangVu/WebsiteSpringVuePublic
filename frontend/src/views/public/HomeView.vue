<template>
  <div class="home-view">
    <!-- 1. Hero Banner Section -->
    <!-- Loại bỏ text-white -->
    <section class="hero-banner text-center d-flex align-items-center justify-content-center">
      <div class="hero-content">
        <h1 class="display-4 fw-bold mb-3 animate-fade-in-up">Tinh Hoa Tượng Gỗ Phong Thủy</h1>
        <p class="lead mb-4 animate-fade-in-up animate-delay-1">
          Nơi hội tụ những tác phẩm điêu khắc tinh xảo, mang lại vượng khí và bình an.
        </p>
        <router-link
          :to="{ name: 'productList' }"
          class="btn btn-lg btn-accent animate-fade-in-up animate-delay-2"
        >
          Khám Phá Bộ Sưu Tập
          <i class="bi bi-arrow-right ms-2"></i>
        </router-link>
      </div>
    </section>

    <!-- 2. Featured Categories Section -->
    <section class="featured-categories container py-5">
      <h2 class="text-center mb-4 section-title"><span>Danh Mục Nổi Bật</span></h2>
      <div v-if="loading.categories" class="text-center">
        <!-- text-primary sẽ tự đổi màu -->
        <div class="spinner-border text-primary"></div>
      </div>
      <div v-else-if="errors.categories" class="alert alert-warning">{{ errors.categories }}</div>
      <div
        v-else-if="categories.length > 0"
        class="row row-cols-2 row-cols-sm-3 row-cols-lg-5 g-3 g-lg-4 justify-content-center"
      >
        <div
          v-for="category in categories.slice(0, 5)"
          :key="category.id"
          class="col category-card-wrapper"
        >
          <router-link
            :to="{ name: 'productListByCategory', params: { categorySlug: category.slug } }"
            class="card category-card text-center h-100 shadow-sm text-decoration-none"
          >
            <img
              :src="category.imageUrl"
              class="card-img-top category-image"
              :alt="category.name"
              @error="onCatImageError"
              loading="lazy"
            />
            <div class="card-body d-flex align-items-center justify-content-center">
              <h5 class="card-title category-name mb-0">{{ category.name }}</h5>
            </div>
          </router-link>
        </div>
      </div>
      <!-- text-muted sẽ tự đổi màu -->
      <div v-else class="text-center text-muted">Chưa có danh mục nào.</div>
    </section>

    <!-- 3. New Arrivals Section -->
    <!-- Loại bỏ bg-light -->
    <section class="new-arrivals py-5">
      <div class="container">
        <h2 class="text-center mb-4 section-title"><span>Sản Phẩm Mới Nhất</span></h2>
        <div v-if="loading.products" class="text-center">
          <div class="spinner-border text-primary"></div>
        </div>
        <div v-else-if="errors.products" class="alert alert-warning">{{ errors.products }}</div>
        <div
          v-else-if="products.length > 0"
          class="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4"
        >
          <ProductCard v-for="product in products" :key="product.id" :product="product" />
        </div>
        <div v-else class="text-center text-muted">Chưa có sản phẩm mới nào.</div>
        <div class="text-center mt-4" v-if="products.length > 0">
          <!-- btn-outline-primary sẽ tự đổi màu -->
          <router-link :to="{ name: 'productList' }" class="btn btn-outline-primary">
            Xem Tất Cả Sản Phẩm
            <i class="bi bi-arrow-right ms-1"></i>
          </router-link>
        </div>
      </div>
    </section>

    <!-- 4. Why Choose Us Section -->
    <section class="why-choose-us container py-5">
      <h2 class="text-center mb-4 section-title"><span>Tại sao nên chọn chúng tôi?</span></h2>
      <div class="row text-center g-4">
        <div class="col-md-4">
          <div class="feature-item feature-item-quality">
            <div class="feature-content">
              <!-- text-primary sẽ tự đổi màu -->
              <i class="bi bi-gem feature-icon text-primary mb-3"></i>
              <h5 class="fw-semibold">Chất Lượng Tuyệt Hảo</h5>
              <!-- text-muted sẽ tự đổi màu -->
              <p class="text-muted small">
                Sử dụng gỗ quý tự nhiên, chế tác tỉ mỉ bởi nghệ nhân lành nghề.
              </p>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="feature-item feature-item-fengshui">
            <div class="feature-content">
              <i class="bi bi-shield-check feature-icon text-primary mb-3"></i>
              <h5 class="fw-semibold">Ý Nghĩa Phong Thủy</h5>
              <p class="text-muted small">
                Mỗi tác phẩm đều chứa đựng năng lượng tích cực, mang lại may mắn, tài lộc.
              </p>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="feature-item feature-item-support">
            <div class="feature-content">
              <i class="bi bi-headset feature-icon text-primary mb-3"></i>
              <h5 class="fw-semibold">Tư Vấn Tận Tâm</h5>
              <p class="text-muted small">
                Hỗ trợ khách hàng lựa chọn vật phẩm phù hợp với nhu cầu và không gian.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { RouterLink } from "vue-router";
import { getAllCategories } from "@/http/modules/public/categoryService.js";
import { getProducts } from "@/http/modules/public/productService.js";
import ProductCard from "@/components/product/ProductCard.vue";
import defaultCategoryImage from "@/assets/images/placeholder.png";

const loading = reactive({ categories: false, products: false });
const errors = reactive({ categories: null, products: null });

const categories = ref([]);
const products = ref([]);

const defaultCategoryImages = [
  "https://phapduyen.com/wp-content/uploads/2018/03/T%C6%B0%E1%BB%A3ng-Ph%E1%BA%ADt-Di-L%E1%BA%B7c-Ng%E1%BB%93i-B%E1%BA%B1ng-%C4%90%E1%BB%93ng-Cao-38-%E2%80%93-50cm-800x800.jpg",
  "https://anthomedecor.vn/wp-content/uploads/2024/09/tuong-quan-cong-3-768x768-1.jpg",
  "https://tuongdong.com.vn/uploads/products/tuong-phat-ba-quan-am-ngoi-dai-sen-ma-vang-24k-td560.jpg",
  "https://naty.vn/wp-content/uploads/2023/07/ty-huu-phong-thuy-8.jpg",
  "https://aiva.com.vn/wp-content/uploads/2023/04/khai-quang-coc-ngam-tien-12.jpg",
];

const fetchCategories = async () => {
  loading.categories = true;
  errors.categories = null;
  try {
    const response = await getAllCategories();
    const fetchedCategories = response.data || [];

    fetchedCategories.forEach((category, index) => {
      if (defaultCategoryImages.length > 0) {
        category.imageUrl = defaultCategoryImages[index % defaultCategoryImages.length];
      } else {
        category.imageUrl = defaultCategoryImage;
      }
    });

    categories.value = fetchedCategories;
  } catch (err) {
    console.error("HomeView: Error fetching categories:", err);
    errors.categories = "Lỗi tải danh mục.";
  } finally {
    loading.categories = false;
  }
};

const fetchNewestProducts = async () => {
  loading.products = true;
  errors.products = null;
  try {
    const params = {
      page: 0,
      size: 8,
      sort: "createdAt,desc",
    };
    const response = await getProducts(params);
    products.value = response.data?.content || [];
  } catch (err) {
    console.error("HomeView: Error fetching newest products:", err);
    errors.products = "Lỗi tải sản phẩm mới.";
  } finally {
    loading.products = false;
  }
};

const onCatImageError = (event) => {
  console.warn("Failed to load category image, using placeholder:", event.target.src);
  event.target.src = defaultCategoryImage;
};

onMounted(() => {
  fetchCategories();
  fetchNewestProducts();
});
</script>

<style scoped>
.home-view {
  width: 100%;
  background-color: var(--bg-secondary);
}

/* --- Hero Banner --- */
.hero-banner {
  position: relative;
  overflow: hidden;
  min-height: 70vh;
  padding: 4rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  /* Màu chữ sẽ được set trong h1, p */
  cursor: pointer;
  border-radius: 0.5rem; /* Có thể bỏ nếu không cần */
}

.hero-banner::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)),
    url("https://klpt.org/wp-content/uploads/2020/06/Tham-quan-c%E1%BB%ADa-h%C3%A0ng-X%C6%B0%E1%BB%9Fng-T%C6%B0%E1%BB%A3ng-G%E1%BB%97.jpg")
      no-repeat center center;
  background-size: cover;
  transition: transform 0.4s ease-out, filter 0.4s ease-out;
}

.hero-banner:hover::before {
  transform: scale(1.1);
  filter: brightness(1.1);
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
}

.hero-banner h1 {
  white-space: nowrap;
  color: var(--text-on-primary-bg); /* Màu chữ trắng/sáng */
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.6);
}

.hero-banner p {
  color: rgba(255, 255, 255, 0.9); /* Giữ nguyên hoặc tạo biến --text-on-image-secondary */
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.btn-accent {
  background-color: var(--accent-color);
  /* Chữ trên nút accent: dùng --bs-dark vì nó sẽ là màu sáng trong dark mode */
  color: var(--bs-dark);
  border: none;
  padding: 0.8rem 2rem;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: var(--bs-box-shadow-md); /* Sử dụng biến shadow Bootstrap */
}

.btn-accent:hover {
  background-color: var(--accent-color-hover);
  color: var(--text-on-primary-bg); /* Màu chữ trắng/sáng khi hover */
  transform: translateY(-2px);
  box-shadow: var(--bs-box-shadow-lg); /* Sử dụng biến shadow Bootstrap */
}

/* --- Section Title --- */
.section-title {
  position: relative;
  display: inline-block;
  padding-bottom: 0.5rem;
  margin-bottom: 2.5rem !important;
  font-weight: 700;
  color: var(--text-primary); /* Màu tiêu đề chính */
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: var(--accent-color); /* Dùng màu nhấn */
  border-radius: 2px;
}

/* --- Featured Categories --- */
.category-card-wrapper {
  transition: transform 0.2s ease-out;
}

.category-card-wrapper:hover {
  transform: scale(1.03);
}

.category-card {
  border: none;
  border-radius: var(--border-radius-lg, 0.5rem);
  transition: all 0.3s ease;
  background-color: var(--card-bg); /* Nền card */
  overflow: hidden;
}

.category-card .card-body {
  padding: 1rem;
}

.category-name {
  color: var(--text-primary); /* Màu chữ chính */
  font-weight: 600;
  font-size: 1rem;
  transition: color 0.2s ease;
}

.category-card:hover .category-name {
  color: var(--bs-primary); /* Đổi màu primary khi hover */
}

.category-image {
  object-fit: cover;
  border-bottom: 1px solid var(--border-color-subtle); /* Viền mờ */
  transition: transform 0.3s ease;
}

.category-card:hover .category-image {
  transform: scale(1.05);
}

/* --- New Arrivals --- */
.new-arrivals {
  background-color: var(--bg-secondary); /* Nền phụ */
}

/* --- Why Choose Us --- */
.feature-icon {
  font-size: 3rem;
  display: inline-block;
  line-height: 1;
  color: var(--bs-primary) !important; /* Màu primary */
}

.feature-item {
  padding: 2rem 1.5rem;
  border-radius: var(--border-radius-lg, 0.5rem);
  background-color: var(--card-bg); /* Nền giống card */
  box-shadow: var(--bs-box-shadow-sm); /* Shadow Bootstrap */
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: box-shadow 0.3s ease;
}

.feature-item:hover {
  box-shadow: var(--bs-box-shadow-md); /* Shadow Bootstrap */
}

.feature-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center center;
  opacity: 0.3;
  z-index: 0;
  border-radius: inherit;
  transition: opacity 0.4s ease;
}

.feature-item:hover::before {
  opacity: 0.15;
}

.feature-content {
  position: relative;
  z-index: 1;
}

/* Ảnh nền cho feature items */
.feature-item-quality::before {
  background-image: url("https://media.istockphoto.com/id/501277671/photo/since-opportunity-didnt-knock-he-decided-to-build-a-door.jpg?s=612x612&w=0&k=20&c=2xGM5620uwHWpQsS52Sb_v1le8ChM3AYIL9OOjSzVSM=");
}

.feature-item-fengshui::before {
  background-image: url("https://cdn-thumbs.ohmyprints.net/1/a5afe8ed23d80dce2d6da9fff45ca87b/130x130/dynamic-size/fit/7dd3364103ff01c6650d83bb39a1091c4ff63c3a07711790d74dbf599badd5a2.jpg");
}

.feature-item-support::before {
  background-image: url("https://images.unsplash.com/photo-1516321497487-e288fb19713f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8c3VwcG9ydHxlbnwwfHwwfHw%3D&w=1000&q=80");
}

.feature-icon {
  font-size: 2.8rem;
  display: inline-block;
  line-height: 1;
  /* color đã được xử lý bởi text-primary class */
  margin-bottom: 1rem !important;
}

.feature-item h5 {
  color: var(--text-primary); /* Màu chữ chính */
  margin-bottom: 0.75rem;
}

.feature-item p {
  color: var(--text-secondary); /* Màu chữ phụ */
  font-size: 0.875rem;
  line-height: 1.6;
}

/* --- Animations --- */
.animate-fade-in-up {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.8s ease-out forwards;
}

.animate-delay-1 {
  animation-delay: 0.2s;
}

.animate-delay-2 {
  animation-delay: 0.4s;
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
