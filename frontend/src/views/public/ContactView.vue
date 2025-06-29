<template>
  <div class="contact-view container mt-5">
    <h1 class="text-center mb-4">Liên Hệ Với Chúng Tôi</h1>

    <div class="row g-4">
      <!-- Cột Thông tin liên hệ -->
      <div class="col-md-6">
        <div class="card h-100">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Thông Tin Cửa Hàng</h5>
          </div>
          <div class="card-body">
            <ul class="list-unstyled mb-0">
              <li class="mb-3 d-flex align-items-start">
                <i class="bi bi-geo-alt-fill me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Địa chỉ:</strong><br />
                  Số 123, Đường Vành Đai 1, Phường Lê Lai, Quận Hoàn Kiếm, Hà Nội
                  <span class="text-muted"></span>
                </div>
              </li>
              <li class="mb-3 d-flex align-items-start">
                <i class="bi bi-telephone-fill me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Điện thoại:</strong><br />
                  <a href="tel:0987654321">098.765.4321</a> <span class="text-muted">Hoàng Vũ</span>
                </div>
              </li>
              <li class="mb-3 d-flex align-items-start">
                <i class="bi bi-envelope-fill me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Email:</strong><br />
                  <a href="mailto:hoangvdph">tuonggophongthuy@gmail.com</a>
                </div>
              </li>
              <li class="mb-3 d-flex align-items-start">
                <i class="bi bi-clock-fill me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Giờ làm việc:</strong><br />
                  Thứ 2 - Thứ 7: 8:00 - 18:00<br />
                  Chủ nhật: Nghỉ
                </div>
              </li>
              <li class="mb-3 d-flex align-items-start">
                <i class="bi bi-facebook me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Facebook:</strong><br />
                  <a href="https://facebook.com" target="_blank" rel="noopener noreferrer"
                    >facebook.com</a
                  >
                </div>
              </li>
              <li class="d-flex align-items-start">
                <i class="bi bi-zalo me-3 fs-4 text-primary"></i>
                <div>
                  <strong>Zalo OA:</strong><br />
                  <a href="https://zalo.me/your_oa_id" target="_blank" rel="noopener noreferrer"
                    >Tượng Gỗ Phong Thủy</a
                  >
                  <span class="text-muted">(0987654321)</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Cột Bản đồ (Tùy chọn) -->
      <div class="col-md-6">
        <div class="card h-100">
          <div class="card-header bg-secondary text-white">
            <h5 class="mb-0">Tìm Chúng Tôi Trên Bản Đồ</h5>
          </div>
          <div class="card-body p-0">
            <!-- p-0 để iframe chiếm toàn bộ card body -->
            <!-- Nhúng Google Maps Iframe -->
            <iframe
              src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3724.096961832421!2d105.8427467153491!3d21.02882739299393!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3135ab9bd9861ca1%3A0xe7887f7b72ca16f0!2zSG_DoG4gS2nhur9tLCBIw6AgTuG7mWksIFZp4buHdCBOYW0!5e0!3m2!1svi!2s!4v1678886666666!5m2!1svi!2s"
              width="100%"
              height="450"
              style="border: 0"
              allowfullscreen=""
              loading="lazy"
              referrerpolicy="no-referrer-when-downgrade"
              title="Bản đồ vị trí cửa hàng"
            >
            </iframe>
          </div>
        </div>
      </div>
    </div>

    <!-- (Tùy chọn) Form liên hệ đơn giản -->
    <div class="row mt-5">
      <div class="col-lg-8 offset-lg-2">
        <h2 class="text-center mb-4">Gửi Tin Nhắn Cho Chúng Tôi</h2>
        <form @submit.prevent="submitContactForm">
          <div class="row mb-3">
            <div class="col-md-6">
              <label for="contactName" class="form-label"
                >Họ và Tên <span class="text-danger">*</span></label
              >
              <input
                type="text"
                class="form-control"
                id="contactName"
                v-model="contactForm.name"
                required
              />
            </div>
            <div class="col-md-6">
              <label for="contactEmail" class="form-label"
                >Email <span class="text-danger">*</span></label
              >
              <input
                type="email"
                class="form-control"
                id="contactEmail"
                v-model="contactForm.email"
                required
              />
            </div>
          </div>
          <div class="mb-3">
            <label for="contactSubject" class="form-label">Chủ đề</label>
            <input
              type="text"
              class="form-control"
              id="contactSubject"
              v-model="contactForm.subject"
            />
          </div>
          <div class="mb-3">
            <label for="contactMessage" class="form-label"
              >Nội dung tin nhắn <span class="text-danger">*</span></label
            >
            <textarea
              class="form-control"
              id="contactMessage"
              rows="5"
              v-model="contactForm.message"
              required
            ></textarea>
          </div>
          <div class="text-center">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="isSubmitting">
              <span
                v-if="isSubmitting"
                class="spinner-border spinner-border-sm"
                role="status"
                aria-hidden="true"
              ></span>
              {{ isSubmitting ? "Đang gửi..." : "Gửi Liên Hệ" }}
            </button>
          </div>
          <div
            v-if="submitStatus"
            class="alert mt-3 text-center"
            :class="submitStatus.success ? 'alert-success' : 'alert-danger'"
          >
            {{ submitStatus.message }}
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
// Import Bootstrap Icons CSS nếu chưa import global (thường không cần nếu đã import JS bundle)
// import 'bootstrap-icons/font/bootstrap-icons.css';

const contactForm = ref({
  name: "",
  email: "",
  subject: "",
  message: "",
});
const isSubmitting = ref(false);
const submitStatus = ref(null); // { success: true/false, message: '...' }

const submitContactForm = async () => {
  isSubmitting.value = true;
  submitStatus.value = null;
  console.log("Submitting contact form:", contactForm.value);

  // --- !!! Xử lý gửi form ở đây !!! ---
  // Thông thường bạn sẽ gọi một API backend để xử lý việc gửi email hoặc lưu vào DB
  // Ví dụ (giả lập thành công sau 2 giây):
  try {
    await new Promise((resolve) => setTimeout(resolve, 2000)); // Giả lập gọi API
    // Nếu thành công:
    submitStatus.value = {
      success: true,
      message: "Tin nhắn của bạn đã được gửi thành công! Chúng tôi sẽ liên hệ lại sớm.",
    };
    // Xóa form
    contactForm.value = { name: "", email: "", subject: "", message: "" };
  } catch (error) {
    console.error("Error submitting form:", error);
    submitStatus.value = { success: false, message: "Đã có lỗi xảy ra. Vui lòng thử lại sau." };
  } finally {
    isSubmitting.value = false;
  }
  // ------------------------------------
};
</script>

<style scoped>
.contact-view {
  /* Style nếu cần */
}
.card-header {
  font-weight: bold;
}
.list-unstyled i {
  margin-top: 0.25rem; /* Căn chỉnh icon với dòng đầu tiên của text */
}
.list-unstyled a {
  text-decoration: none;
}
.list-unstyled a:hover {
  text-decoration: underline;
}
iframe {
  display: block; /* Loại bỏ khoảng trắng thừa bên dưới iframe */
}
</style>
