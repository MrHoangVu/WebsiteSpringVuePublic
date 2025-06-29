<template>
  <div class="admin-shipping-method-form-view">
    <!-- Back Button -->
    <div class="mb-3">
      <router-link :to="{ name: 'adminShippingMethodList' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Danh sách PTVC
      </router-link>
    </div>

    <h1 class="mb-4">{{ isEditMode ? 'Chỉnh sửa Phương thức Vận chuyển' : 'Thêm Phương thức Vận chuyển mới' }}</h1>

    <!-- Loading Initial Data (for Edit mode) -->
    <div v-if="loadingInitial" class="text-center my-5"> ... </div>

    <!-- Error Loading Initial Data -->
    <div v-else-if="initialError" class="alert alert-danger"> ... </div>

    <!-- Form -->
    <form v-else @submit.prevent="handleSubmit" novalidate>
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <!-- General Error Message -->
          <div v-if="submitError" class="alert alert-danger">{{ submitError }}</div>

          <div class="row g-3">
            <!-- Name -->
            <div class="col-md-6">
              <label for="methodName" class="form-label">Tên Phương thức <span class="text-danger">*</span></label>
              <input type="text" class="form-control" :class="{'is-invalid': validationErrors.name}" id="methodName" v-model.trim="formData.name" required :disabled="submitting">
              <div class="invalid-feedback">{{ validationErrors.name }}</div>
            </div>

            <!-- Base Cost -->
            <div class="col-md-6">
              <label for="methodBaseCost" class="form-label">Phí cơ bản (VND) <span class="text-danger">*</span></label>
              <input type="number" step="1000" min="0" class="form-control" :class="{'is-invalid': validationErrors.baseCost}" id="methodBaseCost" v-model.number="formData.baseCost" required :disabled="submitting">
              <div class="invalid-feedback">{{ validationErrors.baseCost }}</div>
            </div>

            <!-- Description -->
            <div class="col-12">
              <label for="methodDescription" class="form-label">Mô tả</label>
              <textarea class="form-control" id="methodDescription" rows="3" v-model="formData.description" :disabled="submitting"></textarea>
            </div>

            <!-- Estimated Days -->
            <div class="col-md-6">
              <label for="methodEstMin" class="form-label">Số ngày dự kiến (Tối thiểu)</label>
              <input type="number" min="0" class="form-control" :class="{'is-invalid': validationErrors.estimatedDaysMin}" id="methodEstMin" v-model.number="formData.estimatedDaysMin" :disabled="submitting">
              <div class="invalid-feedback">{{ validationErrors.estimatedDaysMin }}</div>
            </div>
            <div class="col-md-6">
              <label for="methodEstMax" class="form-label">Số ngày dự kiến (Tối đa)</label>
              <input type="number" min="0" class="form-control" :class="{'is-invalid': validationErrors.estimatedDaysMax || validationErrors.maxDaysValid}" id="methodEstMax" v-model.number="formData.estimatedDaysMax" :disabled="submitting">
              <div class="invalid-feedback">{{ validationErrors.estimatedDaysMax || validationErrors.maxDaysValid }}</div>
            </div>

            <!-- Is Active Status -->
            <div class="col-12">
              <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch" id="methodIsActive" v-model="formData.isActive" :disabled="submitting">
                <label class="form-check-label" for="methodIsActive">
                  {{ formData.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                </label>
              </div>
            </div>
          </div>

          <!-- Submit Button -->
          <hr class="my-4">
          <div class="d-flex justify-content-end">
            <router-link :to="{ name: 'adminShippingMethodList' }" class="btn btn-outline-secondary me-2" :disabled="submitting">Hủy</router-link>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ submitting ? 'Đang lưu...' : (isEditMode ? 'Lưu thay đổi' : 'Thêm Phương thức') }}
            </button>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { createAdminShippingMethod, updateAdminShippingMethod, getAdminShippingMethodById } from '@/http/modules/admin/adminShippingService.js';

const props = defineProps({
  id: { // Nhận ID từ route param khi edit
    type: [String, Number],
    default: null
  }
});

const route = useRoute();
const router = useRouter();

const isEditMode = computed(() => !!props.id);

// Form Data
const formData = reactive({
  name: '',
  description: '',
  baseCost: null,
  estimatedDaysMin: null,
  estimatedDaysMax: null,
  isActive: true,
});

// Form State
const loadingInitial = ref(false);
const initialError = ref(null);
const submitting = ref(false);
const submitError = ref(null);
const validationErrors = reactive({});

// Fetch Data for Edit Mode
const fetchMethodData = async () => {
  if (!isEditMode.value) return;
  loadingInitial.value = true;
  initialError.value = null;
  try {
    const response = await getAdminShippingMethodById(props.id);
    const method = response.data;
    // Map data to formData
    formData.name = method.name || '';
    formData.description = method.description || '';
    formData.baseCost = method.baseCost;
    formData.estimatedDaysMin = method.estimatedDaysMin;
    formData.estimatedDaysMax = method.estimatedDaysMax;
    formData.isActive = method.isActive ?? true;
  } catch (err) {
    console.error(`Error fetching shipping method data for ID ${props.id}:`, err);
    initialError.value = err.response?.data?.message || "Không thể tải dữ liệu phương thức vận chuyển.";
  } finally {
    loadingInitial.value = false;
  }
};

// Client-side Validation (Basic)
const validateForm = () => {
  Object.keys(validationErrors).forEach(key => delete validationErrors[key]);
  let isValid = true;
  if (!formData.name) { validationErrors.name = 'Tên phương thức là bắt buộc.'; isValid = false; }
  if (formData.baseCost === null || formData.baseCost === undefined || formData.baseCost < 0) {
    validationErrors.baseCost = 'Phí cơ bản phải là số không âm.'; isValid = false;
  }
  if (formData.estimatedDaysMin !== null && formData.estimatedDaysMin < 0) {
    validationErrors.estimatedDaysMin = 'Số ngày không được âm.'; isValid = false;
  }
  if (formData.estimatedDaysMax !== null && formData.estimatedDaysMax < 0) {
    validationErrors.estimatedDaysMax = 'Số ngày không được âm.'; isValid = false;
  }
  if (formData.estimatedDaysMin !== null && formData.estimatedDaysMax !== null && formData.estimatedDaysMax < formData.estimatedDaysMin) {
    validationErrors.maxDaysValid = 'Số ngày tối đa phải lớn hơn hoặc bằng tối thiểu.'; isValid = false;
  }
  return isValid;
};

// Form Submit Handler
const handleSubmit = async () => {
  submitError.value = null;
  if (!validateForm()) return;

  submitting.value = true;
  const payload = { ...formData };
  // Chuyển đổi giá trị rỗng thành null cho số ngày nếu cần
  payload.estimatedDaysMin = payload.estimatedDaysMin === '' ? null : payload.estimatedDaysMin;
  payload.estimatedDaysMax = payload.estimatedDaysMax === '' ? null : payload.estimatedDaysMax;


  try {
    if (isEditMode.value) {
      await updateAdminShippingMethod(props.id, payload);
    } else {
      await createAdminShippingMethod(payload);
    }
    router.push({ name: 'adminShippingMethodList' }); // Redirect về list
  } catch (err) {
    console.error("Error submitting shipping method form:", err);
    submitError.value = err.response?.data?.message || `Đã có lỗi xảy ra.`;
    // Xử lý lỗi validation từ backend nếu có (ví dụ: tên trùng)
    if (err.response?.status === 400 && err.response?.data?.errors) {
      // Map lỗi chi tiết vào validationErrors
    }
    if (err.response?.status === 409) { // Conflict - Tên bị trùng
      validationErrors.name = err.response?.data?.message || "Tên phương thức vận chuyển đã tồn tại.";
    }
  } finally {
    submitting.value = false;
  }
};

// Fetch initial data on mount if in edit mode
onMounted(() => {
  if (isEditMode.value) {
    fetchMethodData();
  }
});
</script>

<style scoped>
.is-invalid ~ .invalid-feedback { display: block; }
</style>
