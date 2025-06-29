<template>
  <div class="checkout-view container mt-4 mb-5">
    <h1 class="text-center mb-4">Thanh Toán Đơn Hàng</h1>

    <!-- Loading States -->
    <div v-if="loadingInfo || loadingShipping" class="text-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải thông tin...</span>
      </div>
      <p v-if="loadingInfo" class="mt-2 small text-muted">Đang tải thông tin tài khoản...</p>
      <p v-if="loadingShipping" class="mt-2 small text-muted">Đang tải phương thức vận chuyển...</p>
    </div>

    <!-- Error States -->
    <div v-else-if="errorInfo || errorShipping" class="alert alert-danger">
      <p v-if="errorInfo">Lỗi tải thông tin tài khoản: {{ errorInfo }}</p>
      <p v-if="errorShipping" :class="{ 'mt-2': errorInfo }">
        Lỗi tải phương thức vận chuyển: {{ errorShipping }}
      </p>
    </div>

    <!-- Main Checkout Form & Summary -->
    <form v-else @submit.prevent="handlePlaceOrder" novalidate>
      <div class="row g-4">
        <!-- Left Column: Shipping & Payment -->
        <div class="col-lg-7">
          <!-- 1. Shipping Information -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><h5 class="mb-0">1. Thông tin giao hàng</h5></div>
            <div class="card-body">
              <!-- Address Selection (Logged-in users with addresses) -->
              <div v-if="!isGuest && checkoutInfo?.savedAddresses?.length > 0">
                <div class="mb-3">
                  <label class="form-label fw-medium">Chọn địa chỉ đã lưu:</label>
                  <div
                    v-for="addr in checkoutInfo.savedAddresses"
                    :key="addr.id"
                    class="form-check mb-2"
                  >
                    <input
                      class="form-check-input"
                      type="radio"
                      name="shippingAddressOption"
                      :id="'addr-' + addr.id"
                      :value="addr.id"
                      v-model="selectedAddressId"
                      @change="useNewAddress = false"
                      :disabled="placingOrder"
                    />
                    <label class="form-check-label" :for="'addr-' + addr.id">
                      <strong>{{ addr.recipientName }}</strong> - {{ addr.recipientPhone }}<br />
                      {{ addr.streetAddress }}, {{ addr.ward ? addr.ward + ", " : ""
                      }}{{ addr.district }}, {{ addr.city }}
                      <span v-if="addr.isDefaultShipping" class="badge bg-secondary ms-1"
                        >Mặc định</span
                      >
                    </label>
                  </div>
                </div>
                <div class="mb-3">
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary"
                    @click="toggleNewAddress"
                    :disabled="placingOrder"
                  >
                    {{ useNewAddress ? "Sử dụng địa chỉ đã lưu" : "Hoặc nhập địa chỉ mới" }}
                  </button>
                </div>
              </div>

              <!-- New Address Form (Guests or Logged-in choosing 'new') -->
              <div
                v-if="
                  isGuest || useNewAddress || (!isGuest && !checkoutInfo?.savedAddresses?.length)
                "
              >
                <h6 v-if="!isGuest && checkoutInfo?.savedAddresses?.length > 0" class="mb-3">
                  Nhập địa chỉ mới:
                </h6>
                <div class="row g-3">
                  <div class="col-md-6">
                    <label for="recipientName" class="form-label"
                      >Họ tên người nhận <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.recipientName }"
                      id="recipientName"
                      v-model.trim="shippingAddressInput.recipientName"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.recipientName }}</div>
                  </div>
                  <div class="col-md-6">
                    <label for="recipientPhone" class="form-label"
                      >Số điện thoại <span class="text-danger">*</span></label
                    >
                    <input
                      type="tel"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.recipientPhone }"
                      id="recipientPhone"
                      v-model.trim="shippingAddressInput.recipientPhone"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.recipientPhone }}</div>
                  </div>
                  <!-- Email for Guest -->
                  <div class="col-12" v-if="isGuest">
                    <label for="recipientEmail" class="form-label"
                      >Email <span class="text-danger">*</span></label
                    >
                    <input
                      type="email"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.recipientEmail }"
                      id="recipientEmail"
                      v-model.trim="shippingAddressInput.recipientEmail"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.recipientEmail }}</div>
                  </div>
                  <div class="col-12">
                    <label for="streetAddress" class="form-label"
                      >Địa chỉ cụ thể (Số nhà, tên đường) <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.streetAddress }"
                      id="streetAddress"
                      v-model.trim="shippingAddressInput.streetAddress"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.streetAddress }}</div>
                  </div>
                  <div class="col-md-4">
                    <label for="city" class="form-label"
                      >Tỉnh/Thành phố <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.city }"
                      id="city"
                      v-model.trim="shippingAddressInput.city"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.city }}</div>
                  </div>
                  <div class="col-md-4">
                    <label for="district" class="form-label"
                      >Quận/Huyện <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.district }"
                      id="district"
                      v-model.trim="shippingAddressInput.district"
                      required
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.district }}</div>
                  </div>
                  <div class="col-md-4">
                    <label for="ward" class="form-label">Phường/Xã</label>
                    <input
                      type="text"
                      class="form-control"
                      :class="{ 'is-invalid': validationErrors.ward }"
                      id="ward"
                      v-model.trim="shippingAddressInput.ward"
                      :disabled="placingOrder"
                    />
                    <div class="invalid-feedback">{{ validationErrors.ward }}</div>
                  </div>
                </div>
              </div>
              <div v-if="validationErrors.addressSelection" class="text-danger small mt-2">
                {{ validationErrors.addressSelection }}
              </div>
            </div>
          </div>

          <!-- 2. Shipping Method -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><h5 class="mb-0">2. Phương thức vận chuyển</h5></div>
            <div class="card-body">
              <!-- Display available methods -->
              <div
                v-if="!availableShippingMethods || availableShippingMethods.length === 0"
                class="alert alert-warning small"
              >
                Không có phương thức vận chuyển nào khả dụng.
              </div>
              <div v-else>
                <div
                  v-for="method in availableShippingMethods"
                  :key="method.id"
                  class="form-check mb-2"
                >
                  <input
                    class="form-check-input"
                    type="radio"
                    name="shippingMethod"
                    :id="'ship-' + method.id"
                    :value="method.id"
                    v-model="selectedShippingMethodId"
                    required
                    :disabled="placingOrder"
                  />
                  <label class="form-check-label w-100" :for="'ship-' + method.id">
                    <div class="d-flex justify-content-between">
                      <div>
                        <strong>{{ method.name }}</strong>
                        <small v-if="method.description" class="d-block text-muted">{{
                          method.description
                        }}</small>
                        <small v-if="method.estimatedDelivery" class="d-block text-muted"
                          >Dự kiến: {{ method.estimatedDelivery }}</small
                        >
                      </div>
                      <span class="fw-medium ms-2">{{ formatCurrency(method.baseCost) }}</span>
                    </div>
                  </label>
                </div>
                <div v-if="validationErrors.shippingMethodId" class="text-danger small mt-1">
                  {{ validationErrors.shippingMethodId }}
                </div>
              </div>
            </div>
          </div>

          <!-- 3. Payment Method -->
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-light"><h5 class="mb-0">3. Phương thức thanh toán</h5></div>
            <div class="card-body">
              <div class="form-check mb-2">
                <input
                  class="form-check-input"
                  type="radio"
                  name="paymentMethod"
                  id="payment-cod"
                  value="COD"
                  v-model="selectedPaymentMethod"
                  required
                  :disabled="placingOrder"
                />
                <label class="form-check-label" for="payment-cod"
                  ><i class="bi bi-cash-coin me-1"></i> Thanh toán khi nhận hàng (COD)</label
                >
              </div>
              <div class="form-check mb-2">
                <input
                  class="form-check-input"
                  type="radio"
                  name="paymentMethod"
                  id="payment-bank"
                  value="BANK_TRANSFER"
                  v-model="selectedPaymentMethod"
                  required
                  :disabled="placingOrder"
                />
                <label class="form-check-label" for="payment-bank"
                  ><i class="bi bi-bank me-1"></i> Chuyển khoản ngân hàng</label
                >
                <div
                  v-if="selectedPaymentMethod === 'BANK_TRANSFER'"
                  class="alert alert-info small p-2 mt-2"
                >
                  <strong>Thông tin chuyển khoản:</strong><br />
                  Ngân hàng: ABC Bank<br />
                  Số TK: 1234567890<br />
                  Chủ TK: Tượng Gỗ Phong Thủy<br />
                  Nội dung: Mã đơn hàng [Mã đơn hàng sẽ được cấp sau khi đặt]
                </div>
              </div>
              <div class="form-check mb-2">
                <input
                  class="form-check-input"
                  type="radio"
                  name="paymentMethod"
                  id="payment-vnpay"
                  value="VNPAY"
                  v-model="selectedPaymentMethod"
                  required
                  :disabled="placingOrder"
                />
                <label class="form-check-label" for="payment-vnpay"
                  ><i class="bi bi-credit-card me-1"></i> Thanh toán qua VNPAY</label
                >
              </div>
              <div v-if="validationErrors.paymentMethod" class="text-danger small mt-1">
                {{ validationErrors.paymentMethod }}
              </div>
            </div>
          </div>

          <!-- 4. Order Note -->
          <div class="card shadow-sm">
            <div class="card-header bg-light">
              <h5 class="mb-0">4. Ghi chú đơn hàng (tùy chọn)</h5>
            </div>
            <div class="card-body">
              <textarea
                class="form-control"
                id="orderNote"
                rows="3"
                v-model="orderNote"
                placeholder="Thêm ghi chú cho người bán..."
                :disabled="placingOrder"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Right Column: Order Summary -->
        <div class="col-lg-5">
          <div class="card shadow-sm sticky-top" style="top: 80px">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">Tóm tắt đơn hàng</h5>
            </div>
            <div class="card-body">
              <!-- Cart Items -->
              <ul class="list-unstyled mb-3 small">
                <li
                  v-for="item in cartStore.items"
                  :key="item.productId"
                  class="d-flex justify-content-between align-items-center mb-2 border-bottom pb-1"
                >
                  <div class="d-flex align-items-center">
                    <img
                      :src="item.imageUrl || defaultImage"
                      :alt="item.productName"
                      width="40"
                      class="me-2 rounded"
                    />
                    <span
                      >{{ item.productName }}
                      <span class="text-muted">x {{ item.quantity }}</span></span
                    >
                  </div>
                  <span class="fw-medium">{{ formatCurrency(item.price * item.quantity) }}</span>
                </li>
              </ul>
              <hr />
              <!-- Totals -->
              <ul class="list-group list-group-flush mb-3">
                <li class="list-group-item d-flex justify-content-between px-0">
                  <span>Tạm tính:</span>
                  <span>{{ formatCurrency(cartStore.subtotal) }}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between px-0">
                  <span>Phí vận chuyển:</span>
                  <span v-if="selectedShippingMethod">{{ formatCurrency(shippingCost) }}</span>
                  <span v-else class="text-muted">Chọn PTVC</span>
                </li>
                <li
                  v-if="appliedPromotionSummary"
                  class="list-group-item d-flex justify-content-between px-0 text-success"
                >
                  <span>Giảm giá ({{ appliedPromotionSummary.code }}):</span>
                  <span>- {{ formatCurrency(discountAmount) }}</span>
                </li>
                <li
                  class="list-group-item d-flex justify-content-between px-0 fw-bold fs-5 border-top pt-2"
                >
                  <span>Tổng cộng:</span>
                  <span>{{ formatCurrency(finalTotal) }}</span>
                </li>
              </ul>

              <!-- Place Order Error -->
              <div v-if="errorPlaceOrder" class="alert alert-danger">
                {{ errorPlaceOrder }}
              </div>

              <!-- Place Order Button -->
              <div class="d-grid gap-2 mt-4">
                <button
                  type="submit"
                  class="btn btn-danger btn-lg"
                  :disabled="
                    placingOrder ||
                    cartStore.items.length === 0 ||
                    loadingInfo ||
                    loadingShipping ||
                    !availableShippingMethods ||
                    availableShippingMethods.length === 0
                  "
                >
                  <span
                    v-if="placingOrder"
                    class="spinner-border spinner-border-sm me-2"
                    role="status"
                    aria-hidden="true"
                  ></span>
                  {{ placingOrder ? "Đang xử lý..." : "Đặt Hàng Ngay" }}
                </button>
                <!-- <<< NÚT QUAY LẠI GIỎ HÀNG >>> -->
                <router-link :to="{ name: 'shoppingCart' }" class="btn btn-outline-secondary">
                  <i class="bi bi-arrow-left-short"></i> Quay lại Giỏ hàng
                </router-link>

                <p v-if="cartStore.items.length === 0" class="text-danger small text-center mt-2">
                  Giỏ hàng trống, không thể đặt hàng.
                </p>
                <p
                  v-if="
                    availableShippingMethods &&
                    availableShippingMethods.length === 0 &&
                    !loadingShipping
                  "
                  class="text-danger small text-center mt-2"
                >
                  Không có PTVC, không thể đặt hàng.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth.js";
import { useCartStore } from "@/store/cart.js";
import { getCheckoutInfo, placeOrder } from "@/http/modules/public/checkoutService.js";
import { getActiveShippingMethods } from "@/http/modules/public/shippingService.js";
import defaultImage from "@/assets/images/placeholder.png";

const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();

// --- State ---
const checkoutInfo = ref(null); // For user-specific data (addresses)
const shippingMethods = ref([]); // For general shipping methods (guest & user)
const loadingInfo = ref(false); // Loading user checkout info
const errorInfo = ref(null);
const loadingShipping = ref(false); // Loading shipping methods
const errorShipping = ref(null);

// Form State
const selectedAddressId = ref(null);
const useNewAddress = ref(false);
const shippingAddressInput = reactive({
  recipientName: "",
  recipientPhone: "",
  recipientEmail: "", // Only for guest
  streetAddress: "",
  ward: "",
  district: "",
  city: "",
});
const selectedShippingMethodId = ref(null);
const selectedPaymentMethod = ref("COD");
const orderNote = ref("");

// Action State
const placingOrder = ref(false);
const errorPlaceOrder = ref(null);
const validationErrors = reactive({});
const appliedPromotionSummary = ref(null); // Read from sessionStorage

// --- Computed ---
const isGuest = computed(() => !authStore.isAuthenticated);

// Combined computed property for available shipping methods
const availableShippingMethods = computed(() => {
  // Use methods from checkoutInfo if user is logged in and info is loaded
  if (!isGuest.value && checkoutInfo.value?.availableShippingMethods) {
    return checkoutInfo.value.availableShippingMethods;
  }
  // Otherwise, use the generally fetched shippingMethods
  return shippingMethods.value;
});

const selectedShippingMethod = computed(() => {
  if (!selectedShippingMethodId.value || !availableShippingMethods.value) return null;
  return availableShippingMethods.value.find((m) => m.id === selectedShippingMethodId.value);
});

const shippingCost = computed(() => selectedShippingMethod.value?.baseCost || 0);

const discountAmount = computed(() => appliedPromotionSummary.value?.discount || 0);

const finalTotal = computed(() => {
  const total = cartStore.subtotal + shippingCost.value - discountAmount.value;
  return total > 0 ? total : 0; // Ensure total is not negative
});

// --- Methods ---

// Fetch public active shipping methods (for guest and initial load for user)
const fetchShippingMethods = async () => {
  loadingShipping.value = true;
  errorShipping.value = null;
  try {
    const response = await getActiveShippingMethods();
    shippingMethods.value = response.data || [];
    // Auto-select first method if none selected or previous selection is invalid
    if (
      shippingMethods.value.length > 0 &&
      (!selectedShippingMethodId.value ||
        !shippingMethods.value.some((m) => m.id === selectedShippingMethodId.value))
    ) {
      selectedShippingMethodId.value = shippingMethods.value[0].id;
    } else if (shippingMethods.value.length === 0) {
      selectedShippingMethodId.value = null; // Clear selection if no methods available
    }
  } catch (err) {
    console.error("Error fetching shipping methods:", err);
    errorShipping.value = "Lỗi tải phương thức vận chuyển.";
    shippingMethods.value = [];
    selectedShippingMethodId.value = null;
  } finally {
    loadingShipping.value = false;
  }
};

// Fetch user-specific checkout info (addresses, potentially updated shipping methods)
const fetchCheckoutInfoForUser = async () => {
  if (isGuest.value) return; // Only for logged-in users
  loadingInfo.value = true;
  errorInfo.value = null;
  try {
    const response = await getCheckoutInfo();
    checkoutInfo.value = response.data;

    // Override general shipping methods if provided in user-specific info
    if (response.data?.availableShippingMethods) {
      shippingMethods.value = response.data.availableShippingMethods;
    }

    // Auto-select address logic
    if (checkoutInfo.value?.defaultShippingAddress) {
      selectedAddressId.value = checkoutInfo.value.defaultShippingAddress.id;
      useNewAddress.value = false;
    } else if (checkoutInfo.value?.savedAddresses?.length > 0) {
      selectedAddressId.value = checkoutInfo.value.savedAddresses[0].id;
      useNewAddress.value = false;
    } else {
      useNewAddress.value = true; // No saved addresses, must use new input
    }

    // Auto-select first shipping method if none selected or if list changed
    if (
      shippingMethods.value.length > 0 &&
      (!selectedShippingMethodId.value ||
        !shippingMethods.value.some((m) => m.id === selectedShippingMethodId.value))
    ) {
      selectedShippingMethodId.value = shippingMethods.value[0].id;
    } else if (shippingMethods.value.length === 0) {
      selectedShippingMethodId.value = null;
    }
  } catch (err) {
    console.error("Error fetching checkout info:", err);
    errorInfo.value = "Không thể tải thông tin thanh toán tài khoản.";
    // If user info fails, still try to load public shipping methods if not already loaded
    if (shippingMethods.value.length === 0) {
      await fetchShippingMethods();
    }
  } finally {
    loadingInfo.value = false;
  }
};

const toggleNewAddress = () => {
  useNewAddress.value = !useNewAddress.value;
  if (!useNewAddress.value && checkoutInfo.value?.savedAddresses?.length > 0) {
    // Re-select default or first saved address when switching back
    selectedAddressId.value =
      checkoutInfo.value.defaultShippingAddress?.id || checkoutInfo.value.savedAddresses[0].id;
  } else {
    selectedAddressId.value = null; // Clear saved selection when switching to new
  }
  // Clear address validation errors on toggle
  delete validationErrors.addressSelection;
  delete validationErrors.recipientName;
  delete validationErrors.recipientPhone;
  delete validationErrors.recipientEmail;
  delete validationErrors.streetAddress;
  delete validationErrors.city;
  delete validationErrors.district;
  delete validationErrors.ward;
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value || 0);
};

const validateClientForm = () => {
  Object.keys(validationErrors).forEach((key) => delete validationErrors[key]);
  let isValid = true;

  // 1. Validate Address
  if (!isGuest.value && !selectedAddressId.value && !useNewAddress.value) {
    validationErrors.addressSelection = "Vui lòng chọn hoặc nhập địa chỉ giao hàng.";
    isValid = false;
  }
  if (useNewAddress.value || isGuest.value) {
    if (!shippingAddressInput.recipientName) {
      validationErrors.recipientName = "Vui lòng nhập tên người nhận.";
      isValid = false;
    }
    if (!shippingAddressInput.recipientPhone) {
      validationErrors.recipientPhone = "Vui lòng nhập số điện thoại.";
      isValid = false;
    } else if (!/^(0[3|5|7|8|9])+([0-9]{8})\b/.test(shippingAddressInput.recipientPhone)) {
      validationErrors.recipientPhone = "Số điện thoại không hợp lệ.";
      isValid = false;
    }

    if (isGuest.value) {
      if (!shippingAddressInput.recipientEmail) {
        validationErrors.recipientEmail = "Vui lòng nhập email.";
        isValid = false;
      } else if (!/\S+@\S+\.\S+/.test(shippingAddressInput.recipientEmail)) {
        validationErrors.recipientEmail = "Email không hợp lệ.";
        isValid = false;
      }
    }

    if (!shippingAddressInput.streetAddress) {
      validationErrors.streetAddress = "Vui lòng nhập địa chỉ cụ thể.";
      isValid = false;
    }
    if (!shippingAddressInput.district) {
      validationErrors.district = "Vui lòng nhập Quận/Huyện.";
      isValid = false;
    }
    if (!shippingAddressInput.city) {
      validationErrors.city = "Vui lòng nhập Tỉnh/Thành phố.";
      isValid = false;
    }
    // Ward is optional, no validation needed unless required by business logic
  }

  // 2. Validate Shipping Method
  if (!selectedShippingMethodId.value) {
    validationErrors.shippingMethodId = "Vui lòng chọn phương thức vận chuyển.";
    isValid = false;
  }

  // 3. Validate Payment Method
  if (!selectedPaymentMethod.value) {
    validationErrors.paymentMethod = "Vui lòng chọn phương thức thanh toán.";
    isValid = false;
  }

  return isValid;
};

const handlePlaceOrder = async () => {
  errorPlaceOrder.value = null;
  if (!validateClientForm()) {
    // Scroll to the first validation error
    const firstError = document.querySelector(".is-invalid, .text-danger.small");
    firstError?.scrollIntoView({ behavior: "smooth", block: "center" });
    return;
  }

  placingOrder.value = true;

  // Prepare payload, deciding between saved address ID and new address input
  const orderPayload = {
    selectedShippingAddressId:
      !isGuest.value && !useNewAddress.value ? selectedAddressId.value : null,
    shippingAddressInput: isGuest.value || useNewAddress.value ? { ...shippingAddressInput } : null,
    shippingMethodId: selectedShippingMethodId.value,
    paymentMethod: selectedPaymentMethod.value,
    appliedPromotionCode: appliedPromotionSummary.value?.code || null,
    orderNote: orderNote.value || null,
  };

  // Ensure guest payload has email, and user payload doesn't (backend gets it from token)
  if (!isGuest.value && orderPayload.shippingAddressInput) {
    delete orderPayload.shippingAddressInput.recipientEmail;
  }
  if (
    isGuest.value &&
    orderPayload.shippingAddressInput &&
    !orderPayload.shippingAddressInput.recipientEmail
  ) {
    console.error("Guest email missing in payload!"); // Should be caught by validation, but double-check
    errorPlaceOrder.value = "Lỗi: Email khách hàng bị thiếu.";
    placingOrder.value = false;
    return;
  }

  console.log("Placing order with payload:", JSON.stringify(orderPayload, null, 2));

  try {
    const response = await placeOrder(orderPayload);
    const orderSummary = response.data; // { orderId, paymentUrl?, ... }
    console.log("Order placed successfully:", orderSummary);

    // Clear cart in client state and local storage
    cartStore.clearClientCart();

    // Remove used promo code from session storage
    sessionStorage.removeItem("appliedPromo");

    // Handle payment redirection or success page
    if (orderSummary.paymentUrl) {
      // Redirect to external payment gateway
      window.location.href = orderSummary.paymentUrl;
    } else {
      // Navigate to the order success page
      router.push({ name: "orderSuccess", params: { orderId: orderSummary.orderId } });
    }
  } catch (err) {
    console.error("Error placing order:", err);
    placingOrder.value = false; // Ensure button is re-enabled on error
    if (err.response?.data?.message) {
      errorPlaceOrder.value = err.response.data.message;
    } else if (err.response?.status === 400) {
      // Handle specific validation errors from backend if needed
      errorPlaceOrder.value =
        "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại giỏ hàng hoặc thông tin nhập.";
    } else {
      errorPlaceOrder.value = "Đã có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại sau.";
    }
    // Scroll to top to show the error message
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
  // No finally block setting placingOrder to false here, as navigation/redirect happens on success
};

// --- Lifecycle ---
onMounted(async () => {
  // 1. Check if cart is empty
  if (cartStore.items.length === 0) {
    console.warn("Checkout page loaded with empty cart. Redirecting...");
    router.replace({ name: "shoppingCart" });
    return; // Stop further execution
  }

  // 2. Load applied promotion from session storage
  try {
    const savedPromo = sessionStorage.getItem("appliedPromo");
    if (savedPromo) {
      appliedPromotionSummary.value = JSON.parse(savedPromo);
      console.log("Loaded applied promo:", appliedPromotionSummary.value);
    }
  } catch (e) {
    console.error("Could not parse promo from sessionStorage:", e);
    sessionStorage.removeItem("appliedPromo");
  }

  // 3. Fetch data based on user status
  if (isGuest.value) {
    useNewAddress.value = true; // Guests always input new address
    await fetchShippingMethods(); // Fetch public shipping methods
  } else {
    // Logged-in user: fetch both user info and shipping methods
    // Run concurrently for potentially faster load
    await Promise.all([
      fetchCheckoutInfoForUser(),
      fetchShippingMethods(), // Fetch public methods initially, might be overridden by user info
    ]);
  }
});
</script>

<style scoped>
.checkout-view {
  min-height: 80vh; /* Ensure footer doesn't jump up */
}
.sticky-top {
  /* Add offset if you have a fixed navbar */
  top: 80px;
}
/* Add more specific styles if needed */
</style>
