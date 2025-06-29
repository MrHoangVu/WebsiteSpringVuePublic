<template>
  <div class="admin-product-form-view container mt-4 mb-5">
    <!-- Nút Quay lại -->
    <div class="mb-3">
      <router-link :to="{ name: 'adminProductList' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Danh sách Sản phẩm
      </router-link>
    </div>

    <!-- Tiêu đề động -->
    <h1 class="mb-4">{{ pageTitle }}</h1>

    <!-- Loading dữ liệu ban đầu (chỉ khi sửa) -->
    <div v-if="isEditMode && loadingInitial" class="text-center my-5 py-5">
      <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem">
        <span class="visually-hidden">Đang tải dữ liệu...</span>
      </div>
    </div>

    <!-- Lỗi khi tải dữ liệu ban đầu -->
    <div v-else-if="initialError" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ initialError }}
      <button @click="fetchProductData" class="btn btn-sm btn-danger ms-2" v-if="isEditMode">
        Thử lại
      </button>
    </div>

    <!-- Form chính -->
    <form v-else @submit.prevent="handleSubmit" novalidate>
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <!-- Lỗi submit chung -->
          <div v-if="submitError" class="alert alert-danger mb-3">{{ submitError }}</div>

          <div class="row g-3">
            <!-- Tên Sản phẩm -->
            <div class="col-md-6">
              <label for="productName" class="form-label"
                >Tên Sản phẩm <span class="text-danger">*</span></label
              >
              <input
                type="text"
                class="form-control"
                :class="{ 'is-invalid': validationErrors.name }"
                id="productName"
                v-model.trim="formData.name"
                required
                :disabled="submitting"
              />
              <div class="invalid-feedback">{{ validationErrors.name }}</div>
            </div>

            <!-- Danh mục -->
            <div class="col-md-6">
              <label for="productCategory" class="form-label"
                >Danh mục <span class="text-danger">*</span></label
              >
              <select
                class="form-select"
                :class="{ 'is-invalid': validationErrors.categoryId }"
                id="productCategory"
                v-model="formData.categoryId"
                required
                :disabled="submitting || loadingCategories"
              >
                <option value="" disabled>-- Chọn Danh mục --</option>
                <option v-if="loadingCategories" value="" disabled>Đang tải danh mục...</option>
                <!-- Kiểm tra categories có phải mảng trước khi lặp -->
                <template v-if="Array.isArray(categories)">
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </template>
              </select>
              <div v-if="!loadingCategories && categoryError" class="text-danger small mt-1">
                {{ categoryError }}
              </div>
              <div class="invalid-feedback">{{ validationErrors.categoryId }}</div>
            </div>

            <!-- Mô tả -->
            <div class="col-12">
              <label for="productDescription" class="form-label">Mô tả</label>
              <textarea
                class="form-control"
                id="productDescription"
                rows="5"
                v-model="formData.description"
                :disabled="submitting"
              ></textarea>
              <small class="form-text text-muted"
                >Nên sử dụng trình soạn thảo (VD: TinyMCE, CKEditor) để định dạng tốt hơn nếu
                cần.</small
              >
            </div>

            <!-- Giá bán -->
            <div class="col-md-6">
              <label for="productPrice" class="form-label"
                >Giá bán (VND) <span class="text-danger">*</span></label
              >
              <input
                type="number"
                step="1000"
                min="0"
                class="form-control"
                :class="{ 'is-invalid': validationErrors.price }"
                id="productPrice"
                v-model.number="formData.price"
                required
                :disabled="submitting"
              />
              <div class="invalid-feedback">{{ validationErrors.price }}</div>
            </div>

            <!-- Tồn kho -->
            <div class="col-md-6">
              <label for="productStock" class="form-label"
                >Số lượng tồn kho <span class="text-danger">*</span></label
              >
              <input
                type="number"
                min="0"
                step="1"
                class="form-control"
                :class="{ 'is-invalid': validationErrors.stock }"
                id="productStock"
                v-model.number="formData.stock"
                required
                :disabled="submitting"
              />
              <div class="invalid-feedback">{{ validationErrors.stock }}</div>
            </div>

            <!-- Kích thước -->
            <div class="col-md-6">
              <label for="productDimensions" class="form-label">Kích thước</label>
              <input
                type="text"
                class="form-control"
                id="productDimensions"
                v-model.trim="formData.dimensions"
                :disabled="submitting"
              />
            </div>

            <!-- Chất liệu -->
            <div class="col-md-6">
              <label for="productMaterial" class="form-label">Chất liệu</label>
              <input
                type="text"
                class="form-control"
                id="productMaterial"
                v-model.trim="formData.material"
                :disabled="submitting"
              />
            </div>

            <!-- URL Hình ảnh -->
            <div class="col-12">
              <label for="productImageUrl" class="form-label">URL Hình ảnh chính</label>
              <input
                type="url"
                class="form-control"
                :class="{ 'is-invalid': validationErrors.imageUrl }"
                id="productImageUrl"
                v-model.trim="formData.imageUrl"
                :disabled="submitting"
                placeholder="https://example.com/image.jpg"
              />
              <div class="invalid-feedback">{{ validationErrors.imageUrl }}</div>
              <img
                v-if="formData.imageUrl && !imagePreviewError"
                :src="formData.imageUrl"
                alt="Xem trước ảnh"
                class="mt-2 rounded border"
                style="max-height: 100px; max-width: 100%; object-fit: contain"
                @error="onImagePreviewError"
              />
              <small v-if="imagePreviewError && formData.imageUrl" class="text-danger d-block mt-1"
                >Không thể tải ảnh xem trước từ URL.</small
              >
            </div>

            <!-- Trạng thái Active -->
            <div class="col-12">
              <div class="form-check form-switch">
                <input
                  class="form-check-input"
                  type="checkbox"
                  role="switch"
                  id="productIsActive"
                  v-model="formData.isActive"
                  :disabled="submitting"
                />
                <label class="form-check-label" for="productIsActive">
                  {{
                    formData.isActive
                      ? "Đang bán (Hiển thị trên trang web)"
                      : "Ngừng bán (Ẩn khỏi trang web)"
                  }}
                </label>
              </div>
            </div>
          </div>

          <!-- Nút Submit/Hủy -->
          <hr class="my-4" />
          <div class="d-flex justify-content-end">
            <router-link
              :to="{ name: 'adminProductList' }"
              class="btn btn-outline-secondary me-2"
              :class="{ disabled: submitting }"
              >Hủy</router-link
            >
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="submitting || (loadingCategories && !isEditMode)"
            >
              <span
                v-if="submitting"
                class="spinner-border spinner-border-sm me-2"
                role="status"
                aria-hidden="true"
              ></span>
              {{ submitting ? "Đang lưu..." : pageTitle }}
            </button>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { getAllCategories } from "@/http/modules/public/categoryService.js";
import {
  createProduct,
  updateProduct,
  getProductByIdAdmin,
} from "@/http/modules/admin/adminProductService.js";
// Import Toast service nếu bạn dùng (ví dụ: vue-toastification)
// import { useToast } from 'vue-toastification';

// --- Props & Router ---
const props = defineProps({
  productId: {
    type: [String, Number],
    default: null,
  },
});
const route = useRoute();
const router = useRouter();
// const toast = useToast(); // Khởi tạo toast service

// --- Computed Properties ---
const isEditMode = computed(() => props.productId !== null && props.productId !== undefined);
const pageTitle = computed(() => (isEditMode.value ? "Chỉnh sửa Sản phẩm" : "Thêm Sản phẩm mới"));

// --- Reactive State ---
const formData = reactive({
  name: "",
  description: "",
  price: null,
  stock: 0, // Mặc định là 0
  imageUrl: "",
  dimensions: "",
  material: "",
  categoryId: "", // ID của danh mục được chọn
  isActive: true, // Mặc định là true khi thêm mới
});
const categories = ref([]); // Danh sách các danh mục
const validationErrors = reactive({}); // Lưu lỗi validation của từng trường
const imagePreviewError = ref(false); // Lỗi khi xem trước ảnh

// --- Loading & Error States ---
const loadingInitial = ref(false); // Loading dữ liệu sản phẩm khi sửa
const initialError = ref(null); // Lỗi khi loading dữ liệu sản phẩm
const loadingCategories = ref(false); // Loading danh mục
const categoryError = ref(null); // Lỗi khi loading danh mục
const submitting = ref(false); // Đang submit form
const submitError = ref(null); // Lỗi chung khi submit form

// --- Functions ---

// Hàm Reset Form (dùng khi chuyển từ Edit sang New)
const resetForm = () => {
  console.log("Resetting form data");
  Object.assign(formData, {
    name: "",
    description: "",
    price: null,
    stock: 0,
    imageUrl: "",
    dimensions: "",
    material: "",
    categoryId: "",
    isActive: true,
  });
  Object.keys(validationErrors).forEach((key) => delete validationErrors[key]); // Xóa lỗi validation cũ
  submitError.value = null;
  initialError.value = null; // Reset lỗi fetch ban đầu
  imagePreviewError.value = false;
};

// Lấy danh sách danh mục
const fetchCategories = async () => {
  loadingCategories.value = true;
  categoryError.value = null;
  categories.value = []; // Reset trước khi fetch
  try {
    const response = await getAllCategories();
    // Đảm bảo response.data là mảng
    categories.value = Array.isArray(response.data) ? response.data : [];
    if (categories.value.length === 0) {
      categoryError.value =
        "Không có danh mục nào. Vui lòng thêm danh mục trước khi thêm sản phẩm.";
    }
  } catch (err) {
    console.error("Error fetching categories:", err);
    categoryError.value = "Lỗi tải danh mục. Không thể chọn danh mục.";
  } finally {
    loadingCategories.value = false;
  }
};

// Lấy dữ liệu sản phẩm khi ở chế độ sửa
const fetchProductData = async () => {
  // Kiểm tra lại isEditMode vì hàm này có thể được gọi từ watcher
  if (!isEditMode.value) {
    console.log("fetchProductData called but not in edit mode. Resetting form.");
    resetForm(); // Reset form nếu không còn ở chế độ sửa
    return;
  }

  loadingInitial.value = true;
  initialError.value = null;
  resetForm(); // Reset form trước khi load dữ liệu mới
  console.log(`Fetching data for product ID: ${props.productId}`);

  try {
    // Đảm bảo props.productId có giá trị hợp lệ
    if (!props.productId) {
      throw new Error("Product ID is missing for fetching data.");
    }
    const response = await getProductByIdAdmin(props.productId);
    const product = response.data;
    if (!product) {
      throw new Error(`Không tìm thấy sản phẩm với ID ${props.productId}`);
    }
    console.log("Fetched product data:", product);

    // Map dữ liệu vào formData
    formData.name = product.name || "";
    formData.description = product.description || "";
    formData.price = product.price; // Giữ nguyên kiểu number
    formData.stock = product.stock ?? 0;
    formData.imageUrl = product.imageUrl || "";
    formData.dimensions = product.dimensions || "";
    formData.material = product.material || "";
    formData.categoryId = product.category?.id || ""; // Lấy ID, đảm bảo category tồn tại
    formData.isActive = product.isActive ?? true;
    imagePreviewError.value = false; // Reset lỗi ảnh
  } catch (err) {
    console.error(`Error fetching product data for ID ${props.productId}:`, err);
    initialError.value =
      err.response?.data?.message || err.message || "Không thể tải dữ liệu sản phẩm.";
    // Có thể chuyển hướng về list nếu sản phẩm không tồn tại
    if (err.response?.status === 404) {
      // toast.error(`Sản phẩm với ID ${props.productId} không tồn tại.`);
      // router.push({ name: 'adminProductList'});
    }
  } finally {
    loadingInitial.value = false;
  }
};

// Validation phía Client (có thể mở rộng thêm)
const validateForm = () => {
  // Reset lỗi trước khi validate lại
  Object.keys(validationErrors).forEach((key) => delete validationErrors[key]);
  let isValid = true;

  if (!formData.name.trim()) {
    validationErrors.name = "Tên sản phẩm là bắt buộc.";
    isValid = false;
  }
  if (!formData.categoryId) {
    validationErrors.categoryId = "Vui lòng chọn danh mục.";
    isValid = false;
  }
  if (
    formData.price === null ||
    formData.price === undefined ||
    isNaN(Number(formData.price)) ||
    Number(formData.price) <= 0
  ) {
    validationErrors.price = "Giá bán phải là số dương.";
    isValid = false;
  }
  if (
    formData.stock === null ||
    formData.stock === undefined ||
    isNaN(Number(formData.stock)) ||
    Number(formData.stock) < 0 ||
    !Number.isInteger(Number(formData.stock))
  ) {
    validationErrors.stock = "Tồn kho phải là số nguyên không âm.";
    isValid = false;
  }
  if (formData.imageUrl && formData.imageUrl.trim()) {
    // Chỉ validate nếu có URL
    try {
      new URL(formData.imageUrl.trim());
    } catch (_) {
      validationErrors.imageUrl = "URL hình ảnh không hợp lệ.";
      isValid = false;
    }
  }

  // Reset lỗi preview ảnh nếu URL hợp lệ hoặc rỗng
  if (!validationErrors.imageUrl) {
    imagePreviewError.value = false;
  }

  return isValid;
};

// Xử lý lỗi xem trước ảnh
const onImagePreviewError = () => {
  if (formData.imageUrl) {
    // Chỉ báo lỗi nếu thực sự có URL
    console.warn("Could not load image preview for URL:", formData.imageUrl);
    imagePreviewError.value = true;
    validationErrors.imageUrl = "Không thể tải ảnh xem trước từ URL này."; // Thêm lỗi validation
  }
};

// Hàm Submit Form
const handleSubmit = async () => {
  submitError.value = null; // Reset lỗi submit chung
  // Validate trước khi gửi
  if (!validateForm()) {
    console.warn("Client-side validation failed:", validationErrors);
    // Scroll tới trường lỗi đầu tiên để người dùng thấy
    const firstErrorElement = document.querySelector(".is-invalid");
    firstErrorElement?.focus(); // Focus vào input lỗi
    firstErrorElement?.scrollIntoView({ behavior: "smooth", block: "center" });
    // toast.error("Vui lòng kiểm tra lại các trường thông tin.");
    return;
  }

  submitting.value = true; // Bắt đầu trạng thái submitting

  // Tạo payload cuối cùng
  const payload = {
    name: formData.name,
    description: formData.description,
    price: Number(formData.price), // Đảm bảo là số
    stock: Number(formData.stock), // Đảm bảo là số nguyên
    imageUrl: formData.imageUrl.trim() || null, // Gửi null nếu rỗng
    dimensions: formData.dimensions.trim() || null,
    material: formData.material.trim() || null,
    categoryId: Number(formData.categoryId), // Đảm bảo là số
    isActive: formData.isActive,
  };

  console.log("Submitting payload:", payload);
  console.log(`Mode: ${isEditMode.value ? "Edit" : "Create"}, Product ID: ${props.productId}`); // **DEBUG QUAN TRỌNG**

  try {
    let response;
    if (isEditMode.value) {
      // **KIỂM TRA LẠI isEditMode và props.productId trước khi gọi API**
      if (!props.productId) {
        throw new Error("Không tìm thấy ID sản phẩm để cập nhật.");
      }
      console.log(`Calling updateProduct for ID: ${props.productId}`);
      response = await updateProduct(props.productId, payload);
      console.log("Product updated:", response.data);
      // toast.success("Cập nhật sản phẩm thành công!");
    } else {
      console.log("Calling createProduct");
      response = await createProduct(payload);
      console.log("Product created:", response.data);
      // toast.success("Thêm sản phẩm thành công!");
    }

    // Chuyển hướng về trang danh sách sau khi thành công
    router.push({ name: "adminProductList" });
  } catch (err) {
    console.error("Error submitting product form:", err);
    const errorMsg =
      err.response?.data?.message ||
      `Đã có lỗi xảy ra khi ${isEditMode.value ? "cập nhật" : "thêm"} sản phẩm.`;
    submitError.value = errorMsg;
    // Map lỗi validation từ backend (nếu có)
    if (err.response?.status === 400 && err.response?.data?.errors) {
      console.error("Backend validation errors:", err.response.data.errors);
      for (const field in err.response.data.errors) {
        // Cần map tên field từ backend sang tên field trong validationErrors nếu khác nhau
        const clientField = field; // Giả sử tên giống nhau
        validationErrors[clientField] = err.response.data.errors[field];
      }
    } else if (err.response?.status === 409) {
      // Lỗi trùng lặp (ví dụ: slug hoặc name)
      validationErrors.name = err.response?.data?.message || "Tên sản phẩm hoặc slug đã tồn tại.";
    }
    // toast.error(errorMsg);
    window.scrollTo({ top: 0, behavior: "smooth" }); // Cuộn lên đầu để thấy lỗi
  } finally {
    submitting.value = false; // Kết thúc trạng thái submitting
  }
};

// --- Lifecycle Hooks & Watchers ---

// Fetch dữ liệu khi component được mount lần đầu
onMounted(() => {
  console.log("AdminProductFormView Mounted. productId prop:", props.productId); // **DEBUG QUAN TRỌNG**
  fetchCategories(); // Luôn tải danh mục
  if (isEditMode.value) {
    fetchProductData(); // Tải dữ liệu sản phẩm nếu có ID
  }
});

// Watch sự thay đổi của props.productId (khi route thay đổi)
// Điều này cực kỳ quan trọng để xử lý việc chuyển từ trang "Thêm mới" sang "Sửa"
// hoặc từ "Sửa sản phẩm A" sang "Sửa sản phẩm B" mà không cần tải lại toàn bộ trang.
watch(
  () => props.productId,
  (newId, oldId) => {
    console.log(`productId prop changed from ${oldId} to ${newId}`); // **DEBUG QUAN TRỌNG**
    // Chỉ thực hiện hành động nếu ID thực sự thay đổi
    if (newId !== oldId) {
      if (newId) {
        // Nếu có ID mới (chuyển sang/đổi sang chế độ sửa)
        fetchProductData(); // Fetch dữ liệu cho ID mới
      } else {
        // Nếu ID mới là null (chuyển sang chế độ thêm mới)
        resetForm(); // Reset form về trạng thái ban đầu
      }
    }
  },
  { immediate: false }
); // Không chạy immediate vì onMounted đã xử lý lần đầu
</script>

<style scoped>
.admin-product-form-view {
  min-height: 80vh;
}
/* Đảm bảo feedback lỗi hiển thị đúng */
.form-control.is-invalid ~ .invalid-feedback,
.form-select.is-invalid ~ .invalid-feedback {
  display: block;
}
</style>
