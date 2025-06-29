<template>
  <div class="admin-promotion-form-view">
    <!-- Nút Quay lại -->
    <div class="mb-3">
      <router-link :to="{ name: 'adminPromotionList' }" class="btn btn-sm btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> Quay lại Danh sách KM
      </router-link>
    </div>

    <!-- Tiêu đề động -->
    <h1 class="mb-4">{{ isEditMode ? 'Chỉnh sửa Khuyến mãi' : 'Thêm Khuyến mãi mới' }}</h1>

    <!-- Trạng thái Loading dữ liệu ban đầu (chỉ khi sửa) -->
    <div v-if="isEditMode && loadingInitial" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải dữ liệu...</span>
      </div>
    </div>

    <!-- Trạng thái Lỗi khi tải dữ liệu ban đầu (chỉ khi sửa) -->
    <div v-else-if="initialError" class="alert alert-danger">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ initialError }}
    </div>

    <!-- Form chính (hiển thị khi không loading/error hoặc khi thêm mới) -->
    <form v-else @submit.prevent="handleSubmit" novalidate>
      <div class="card shadow-sm">
        <div class="card-body p-4">
          <!-- Thông báo lỗi chung khi submit -->
          <div v-if="submitError" class="alert alert-danger mb-3">{{ submitError }}</div>

          <div class="row g-3">
            <!-- Mã Code -->
            <div class="col-md-6">
              <label for="promoCode" class="form-label">Mã Code <span class="text-danger">*</span></label>
              <input type="text" class="form-control text-uppercase"
                     :class="{ 'is-invalid': validationErrors.code }"
                     id="promoCode"
                     v-model.trim="formData.code" required
                     :disabled="submitting"
                     @input="formData.code = formData.code.toUpperCase().replace(/[^A-Z0-9]/g, '')">
              <div class="form-text">Chỉ chứa chữ cái (viết hoa) và số, không dấu, không khoảng trắng.</div>
              <div v-if="validationErrors.code" class="invalid-feedback">{{ validationErrors.code }}</div>
            </div>

            <!-- Tên Khuyến mãi -->
            <div class="col-md-6">
              <label for="promoName" class="form-label">Tên Khuyến mãi</label>
              <input type="text" class="form-control"
                     :class="{ 'is-invalid': validationErrors.name }"
                     id="promoName" v-model.trim="formData.name"
                     :disabled="submitting">
              <div v-if="validationErrors.name" class="invalid-feedback">{{ validationErrors.name }}</div>
            </div>

            <!-- Mô tả -->
            <div class="col-12">
              <label for="promoDescription" class="form-label">Mô tả</label>
              <textarea class="form-control"
                        :class="{ 'is-invalid': validationErrors.description }"
                        id="promoDescription" rows="3"
                        v-model="formData.description" :disabled="submitting"></textarea>
              <div v-if="validationErrors.description" class="invalid-feedback">{{ validationErrors.description }}</div>
            </div>

            <!-- Loại giảm giá & Giá trị giảm -->
            <div class="col-md-6">
              <label for="promoDiscountType" class="form-label">Loại giảm giá <span class="text-danger">*</span></label>
              <select class="form-select"
                      :class="{ 'is-invalid': validationErrors.discountType }"
                      id="promoDiscountType" v-model="formData.discountType" required
                      :disabled="submitting">
                <option value="PERCENTAGE">Theo Phần trăm (%)</option>
                <option value="FIXED_AMOUNT">Số tiền cố định (VND)</option>
              </select>
              <div v-if="validationErrors.discountType" class="invalid-feedback">{{ validationErrors.discountType }}</div>
            </div>
            <div class="col-md-6">
              <label for="promoDiscountValue" class="form-label">Giá trị giảm <span class="text-danger">*</span></label>
              <input type="number" :step="formData.discountType === 'PERCENTAGE' ? '0.01' : '1000'"
                     min="0" class="form-control"
                     :class="{ 'is-invalid': validationErrors.discountValue || validationErrors.validPercentage }"
                     id="promoDiscountValue" v-model.number="formData.discountValue" required
                     :disabled="submitting">
              <!-- Hiển thị lỗi validation kết hợp -->
              <div v-if="validationErrors.discountValue || validationErrors.validPercentage" class="invalid-feedback">
                {{ validationErrors.discountValue || validationErrors.validPercentage }}
              </div>
            </div>

            <!-- Ngày bắt đầu & Ngày kết thúc -->
            <div class="col-md-6">
              <label for="promoStartDate" class="form-label">Ngày bắt đầu <span class="text-danger">*</span></label>
              <input type="datetime-local" class="form-control"
                     :class="{ 'is-invalid': validationErrors.startDate || validationErrors.endDateAfterStartDate }"
                     id="promoStartDate" v-model="formData.startDate" required
                     :disabled="submitting">
              <!-- Hiển thị lỗi validation kết hợp -->
              <div v-if="validationErrors.startDate || validationErrors.endDateAfterStartDate" class="invalid-feedback">
                {{ validationErrors.startDate || validationErrors.endDateAfterStartDate }}
              </div>
            </div>
            <div class="col-md-6">
              <label for="promoEndDate" class="form-label">Ngày kết thúc <span class="text-danger">*</span></label>
              <input type="datetime-local" class="form-control"
                     :class="{ 'is-invalid': validationErrors.endDate || validationErrors.endDateAfterStartDate }"
                     id="promoEndDate" v-model="formData.endDate" required :disabled="submitting">
              <!-- Chỉ hiển thị lỗi endDate nếu không có lỗi endDateAfterStartDate -->
              <div v-if="validationErrors.endDate && !validationErrors.endDateAfterStartDate" class="invalid-feedback">
                {{ validationErrors.endDate }}
              </div>
              <!-- Hiển thị lỗi endDateAfterStartDate riêng -->
              <div v-if="validationErrors.endDateAfterStartDate" class="invalid-feedback">
                {{ validationErrors.endDateAfterStartDate }}
              </div>
            </div>

            <!-- Lượt sử dụng tối đa & Giá trị đơn tối thiểu -->
            <div class="col-md-6">
              <label for="promoMaxUsage" class="form-label">Lượt sử dụng tối đa</label>
              <input type="number" min="1" step="1" class="form-control"
                     :class="{ 'is-invalid': validationErrors.maxUsage }"
                     id="promoMaxUsage"
                     v-model.number="formData.maxUsage" placeholder="Để trống nếu không giới hạn"
                     :disabled="submitting">
              <div v-if="validationErrors.maxUsage" class="invalid-feedback">{{ validationErrors.maxUsage }}</div>
            </div>
            <div class="col-md-6">
              <label for="promoMinOrderValue" class="form-label">Giá trị đơn tối thiểu (VND)</label>
              <input type="number" min="0" step="1000" class="form-control"
                     :class="{ 'is-invalid': validationErrors.minOrderValue }"
                     id="promoMinOrderValue"
                     v-model.number="formData.minOrderValue"
                     placeholder="Để trống nếu không yêu cầu" :disabled="submitting">
              <div v-if="validationErrors.minOrderValue" class="invalid-feedback">{{ validationErrors.minOrderValue }}</div>
            </div>

            <!-- Bậc Khách hàng mục tiêu -->
            <div class="col-12">
              <label class="form-label mb-2">Áp dụng cho Bậc Khách hàng:</label>
              <div class="d-flex flex-wrap gap-3">
                <!-- Checkbox "Tất cả" -->
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="tierAll"
                         :checked="isAllTiersSelected" @change="toggleAllTiers"
                         :disabled="submitting">
                  <label class="form-check-label fw-medium" for="tierAll">Tất cả</label>
                </div>
                <!-- Checkbox cho từng bậc cụ thể -->
                <div class="form-check" v-for="tier in availableTiers" :key="tier.code">
                  <input class="form-check-input" type="checkbox" :id="'tier-' + tier.code" :value="tier.code"
                         v-model="selectedTiers" :disabled="submitting || isAllTiersSelected">
                  <label class="form-check-label" :for="'tier-' + tier.code">{{ tier.name }}</label>
                </div>
              </div>
              <!-- Lỗi validation (hiếm khi xảy ra với checkbox) -->
              <div v-if="validationErrors.targetTiers" class="text-danger small mt-1">
                {{ validationErrors.targetTiers }}
              </div>
            </div>

            <!-- Trạng thái Hoạt động -->
            <div class="col-12">
              <div class="form-check form-switch mt-2">
                <input class="form-check-input" type="checkbox" role="switch" id="promoIsActive"
                       v-model="formData.isActive" :disabled="submitting">
                <label class="form-check-label" for="promoIsActive">
                  {{ formData.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                </label>
              </div>
            </div>
          </div>

          <!-- Nút Submit và Hủy -->
          <hr class="my-4">
          <div class="d-flex justify-content-end">
            <router-link :to="{ name: 'adminPromotionList' }" class="btn btn-outline-secondary me-2"
                         :class="{ 'disabled': submitting }">Hủy
            </router-link>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              {{ submitting ? 'Đang lưu...' : (isEditMode ? 'Lưu thay đổi' : 'Thêm Khuyến mãi') }}
            </button>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
// Import các service API cần thiết
import {
  createAdminPromotion,
  updateAdminPromotion,
  getAdminPromotionById
} from '@/http/modules/admin/adminPromotionService.js';
// Import hàm formatTier và danh sách các bậc (nếu cần)
import { formatTier, allTiers } from '@/utils/formatters'; // Đảm bảo allTiers được export từ formatters.js

// Định nghĩa props để nhận ID từ route
const props = defineProps({
  id: {
    type: [String, Number], // Chấp nhận cả String và Number
    required: false,       // Không bắt buộc (cho chế độ thêm mới)
    default: null
  }
});

const route = useRoute();
const router = useRouter();

// Xác định chế độ (true nếu có props.id)
const isEditMode = computed(() => props.id !== null && props.id !== undefined);

// Dữ liệu form (dùng reactive)
const formData = reactive({
  code: '',
  name: '',
  description: '',
  discountType: 'PERCENTAGE', // Giá trị mặc định
  discountValue: null,
  startDate: '', // Sẽ format khi fetch và trước khi submit
  endDate: '',   // Sẽ format khi fetch và trước khi submit
  maxUsage: null,
  minOrderValue: null,
  isActive: true, // Mặc định là active khi thêm mới
  // targetTiers sẽ được xử lý riêng
});

// State quản lý việc chọn Bậc KH
const selectedTiers = ref([]); // Mảng chứa các mã bậc được chọn (VD: ['SILVER', 'GOLD'])
const availableTiers = ref(allTiers || []); // Lấy từ formatters hoặc định nghĩa ở đây

// State của component (loading, lỗi, submit)
const loadingInitial = ref(false); // Loading dữ liệu ban đầu khi sửa
const initialError = ref(null);    // Lỗi khi loading dữ liệu ban đầu
const submitting = ref(false);     // Đang trong quá trình submit form
const submitError = ref(null);     // Lỗi chung khi submit form
const validationErrors = reactive({}); // Object chứa lỗi validation cho từng trường

// --- Computed Property cho Checkbox "Tất cả" Bậc KH ---
const isAllTiersSelected = computed(() => selectedTiers.value.length === 0);

// --- Hàm xử lý Checkbox "Tất cả" ---
const toggleAllTiers = (event) => {
  if (event.target.checked) {
    selectedTiers.value = []; // Chọn "Tất cả" -> mảng rỗng
  }
  // Không làm gì khi bỏ chọn "Tất cả", người dùng phải tự chọn bậc cụ thể
};

// --- Watcher cho selectedTiers ---
// Nếu người dùng chọn tất cả các bậc cụ thể, tự động chuyển về trạng thái "Tất cả" (mảng rỗng)
// watch(selectedTiers, (newVal) => {
//   if (availableTiers.value.length > 0 && newVal.length === availableTiers.value.length) {
//     selectedTiers.value = [];
//   }
// }, { deep: true });

// --- Hàm lấy dữ liệu khi ở chế độ Sửa ---
const fetchPromotionData = async () => {
  if (!isEditMode.value) return; // Chỉ chạy khi sửa

  loadingInitial.value = true;
  initialError.value = null;
  console.log(`Fetching data for promotion ID: ${props.id}`);
  try {
    const response = await getAdminPromotionById(props.id);
    const promo = response.data;
    console.log('Fetched promotion data:', promo);

    // Map dữ liệu từ API vào formData và selectedTiers
    formData.code = promo.code || '';
    formData.name = promo.name || '';
    formData.description = promo.description || '';
    formData.discountType = promo.discountType || 'PERCENTAGE';
    formData.discountValue = promo.discountValue;
    // Format ngày giờ từ ISO string (backend trả về) sang định dạng input datetime-local (YYYY-MM-DDTHH:mm)
    formData.startDate = formatDateTimeForInput(promo.startDate);
    formData.endDate = formatDateTimeForInput(promo.endDate);
    formData.maxUsage = promo.maxUsage;
    formData.minOrderValue = promo.minOrderValue;
    // Mặc định isActive là true nếu backend không trả về (dù ít khi xảy ra)
    formData.isActive = promo.isActive === undefined ? true : promo.isActive;
    // Chuyển đổi targetTiers từ chuỗi (VD: "SILVER,GOLD") thành mảng ['SILVER', 'GOLD']
    selectedTiers.value = promo.targetTiers ? promo.targetTiers.split(',').map(t => t.trim()).filter(t => t) : [];

  } catch (err) {
    console.error("Error fetching promotion data:", err);
    initialError.value = `Không thể tải dữ liệu khuyến mãi. Lỗi: ${err.response?.data?.message || err.message}`;
  } finally {
    loadingInitial.value = false;
  }
};

// --- Hàm tiện ích format ngày giờ cho input ---
const formatDateTimeForInput = (dateTimeString) => {
  if (!dateTimeString) return '';
  try {
    const date = new Date(dateTimeString);
    if (isNaN(date.getTime())) return '';
    // Lấy các thành phần và đảm bảo 2 chữ số
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  } catch (e) {
    console.error("Error formatting date for input:", dateTimeString, e);
    return '';
  }
};

// --- Hàm Validation phía Client ---
const validateForm = () => {
  // Reset lỗi trước mỗi lần validate
  Object.keys(validationErrors).forEach(key => delete validationErrors[key]);
  let isValid = true;

  // Code
  if (!formData.code) {
    validationErrors.code = 'Mã code là bắt buộc.'; isValid = false;
  } else if (!/^[A-Z0-9]+$/.test(formData.code)) { // Đã ép uppercase nên chỉ cần check ký tự hợp lệ
    validationErrors.code = 'Mã chỉ được chứa chữ cái (viết hoa) và số.'; isValid = false;
  }

  // Name (Optional, nhưng có thể thêm max length)
  if (formData.name && formData.name.length > 255) {
    validationErrors.name = 'Tên không được vượt quá 255 ký tự.'; isValid = false;
  }

  // Discount Type/Value
  if (!formData.discountType) {
    validationErrors.discountType = 'Loại giảm giá là bắt buộc.'; isValid = false;
  }
  if (formData.discountValue === null || formData.discountValue === undefined) {
    validationErrors.discountValue = 'Giá trị giảm là bắt buộc.'; isValid = false;
  } else if (formData.discountValue <= 0) {
    validationErrors.discountValue = 'Giá trị giảm phải lớn hơn 0.'; isValid = false;
  } else if (formData.discountType === 'PERCENTAGE' && formData.discountValue > 100) {
    validationErrors.validPercentage = 'Giá trị giảm phần trăm không được lớn hơn 100.'; isValid = false;
  }

  // Dates
  if (!formData.startDate) {
    validationErrors.startDate = 'Ngày bắt đầu là bắt buộc.'; isValid = false;
  }
  if (!formData.endDate) {
    validationErrors.endDate = 'Ngày kết thúc là bắt buộc.'; isValid = false;
  } else if (formData.startDate && formData.endDate) {
    try {
      // Chỉ so sánh khi cả hai ngày đều hợp lệ
      const start = new Date(formData.startDate);
      const end = new Date(formData.endDate);
      if (isNaN(start.getTime())) {
        validationErrors.startDate = 'Ngày bắt đầu không hợp lệ.'; isValid = false;
      }
      if (isNaN(end.getTime())) {
        validationErrors.endDate = 'Ngày kết thúc không hợp lệ.'; isValid = false;
      }
      if (!isNaN(start.getTime()) && !isNaN(end.getTime()) && end <= start) {
        validationErrors.endDateAfterStartDate = 'Ngày kết thúc phải sau ngày bắt đầu.'; isValid = false;
      }
    } catch (e) {
      validationErrors.startDate = 'Định dạng ngày giờ không hợp lệ.'; isValid = false;
      validationErrors.endDate = 'Định dạng ngày giờ không hợp lệ.'; isValid = false;
    }
  }


  // Max Usage / Min Order Value (chỉ validate nếu có giá trị)
  if (formData.maxUsage !== null && formData.maxUsage !== '' && ( !Number.isInteger(Number(formData.maxUsage)) || Number(formData.maxUsage) < 1 ) ) {
    validationErrors.maxUsage = 'Lượt sử dụng tối đa phải là số nguyên lớn hơn hoặc bằng 1.'; isValid = false;
  }
  if (formData.minOrderValue !== null && formData.minOrderValue !== '' && Number(formData.minOrderValue) < 0) {
    validationErrors.minOrderValue = 'Giá trị đơn tối thiểu không được âm.'; isValid = false;
  }

  return isValid;
};

// --- Hàm Xử lý Submit Form ---
const handleSubmit = async () => {
  submitError.value = null; // Reset lỗi submit chung
  // Thực hiện validation phía client trước
  if (!validateForm()) {
    console.log("Client-side validation failed:", validationErrors);
    // Scroll lên đầu form hoặc hiển thị thông báo lỗi tổng quát
    window.scrollTo({ top: 0, behavior: 'smooth' });
    return;
  }

  submitting.value = true; // Bắt đầu trạng thái submitting

  // Chuẩn bị payload để gửi lên API
  const payload = {
    ...formData,
    // Chuyển đổi mảng selectedTiers thành chuỗi, phân cách bằng dấu phẩy
    // Nếu mảng rỗng (nghĩa là chọn "Tất cả"), gửi null hoặc chuỗi rỗng tùy yêu cầu backend
    targetTiers: selectedTiers.value.length > 0 ? selectedTiers.value.join(',') : null,
    // Chuyển đổi date từ input (YYYY-MM-DDTHH:mm) về dạng ISO String mà backend mong muốn (nếu cần)
    // Thường thì backend Java có thể tự parse được định dạng này, nhưng để chắc chắn:
    // startDate: formData.startDate ? new Date(formData.startDate).toISOString() : null,
    // endDate: formData.endDate ? new Date(formData.endDate).toISOString() : null,
  };

  console.log("Submitting payload:", payload);

  try {
    let response;
    if (isEditMode.value) {
      console.log(`Calling updateAdminPromotion for ID: ${props.id}`);
      response = await updateAdminPromotion(props.id, payload);
      // TODO: Hiển thị thông báo thành công (toast)
      console.log("Update successful:", response.data);
    } else {
      console.log("Calling createAdminPromotion");
      response = await createAdminPromotion(payload);
      // TODO: Hiển thị thông báo thành công (toast)
      console.log("Create successful:", response.data);
    }
    // Nếu thành công, chuyển hướng về trang danh sách
    router.push({ name: 'adminPromotionList' });

  } catch (err) {
    console.error("Error submitting promotion form:", err);
    // Xử lý lỗi trả về từ backend
    if (err.response) {
      console.error("Backend error response:", err.response.data);
      const errorData = err.response.data;
      // Hiển thị lỗi chung
      submitError.value = errorData.message || `Đã có lỗi xảy ra khi ${isEditMode.value ? 'cập nhật' : 'thêm'} khuyến mãi.`;

      // Xử lý lỗi validation cụ thể từ backend (nếu có)
      if (errorData.errors) {
        // Ví dụ: nếu backend trả về lỗi dạng { field: 'code', message: '...' }
        for (const errorField in errorData.errors) {
          validationErrors[errorField] = errorData.errors[errorField];
        }
      } else if (err.response.status === 409) { // Conflict - Mã code bị trùng
        validationErrors.code = errorData.message || "Mã code này đã tồn tại.";
      }
    } else {
      // Lỗi mạng hoặc lỗi không xác định
      submitError.value = "Đã có lỗi xảy ra. Vui lòng kiểm tra kết nối và thử lại.";
    }
    window.scrollTo({ top: 0, behavior: 'smooth' }); // Cuộn lên đầu để thấy lỗi
  } finally {
    submitting.value = false; // Kết thúc trạng thái submitting
  }
};

// --- Lifecycle Hook ---
onMounted(() => {
  // Tự động fetch dữ liệu nếu đang ở chế độ sửa
  if (isEditMode.value) {
    fetchPromotionData();
  } else {
    // Set giá trị mặc định cho ngày bắt đầu khi thêm mới (ví dụ: thời điểm hiện tại)
    formData.startDate = formatDateTimeForInput(new Date());
  }
});

</script>

<style scoped>
/* Hiển thị feedback lỗi */
.is-invalid ~ .invalid-feedback,
.is-invalid ~ .text-danger {
  display: block;
}

/* Style cho checkbox và label */
.form-check-label {
  cursor: pointer;
  margin-left: 0.25rem;
}
.form-check-input {
  cursor: pointer;
}
.form-switch .form-check-input {
  width: 3em; /* Tăng chiều rộng switch */
}
</style>
