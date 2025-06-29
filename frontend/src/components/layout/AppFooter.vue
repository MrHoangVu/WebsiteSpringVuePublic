<template>
  <footer class="app-footer text-white mt-auto pt-5 pb-4">
    <div class="container">
      <div class="row gy-4 align-items-start">
        <!-- Cột 1: Giới thiệu/Logo -->
        <div class="col-lg-4 col-md-6">
          <div class="d-flex align-items-center mb-3">
            <img
              src="https://doanhnhantredaklak.org/userfiles/users/107/1609494305008.png"
              alt="Logo"
              style="height: 50px"
              class="me-2"
            />
            <h5 class="footer-heading mb-0">Tượng Gỗ Phong Thủy</h5>
          </div>
          <p class="small text-white-50">
            Chuyên cung cấp các sản phẩm tượng gỗ phong thủy mỹ nghệ độc đáo, chất lượng cao, mang
            lại may mắn và tài lộc.
          </p>
        </div>

        <!-- Cột 2: Liên kết hữu ích -->
        <div class="col-lg-2 col-md-6">
          <h5 class="footer-heading">Liên kết</h5>
          <ul class="list-unstyled">
            <li>
              <router-link :to="{ name: 'home' }" class="footer-link">Trang chủ</router-link>
            </li>
            <li>
              <router-link :to="{ name: 'productList' }" class="footer-link">Sản phẩm</router-link>
            </li>
            <li>
              <router-link :to="{ name: 'articleList' }" class="footer-link">Tin tức</router-link>
            </li>
            <li>
              <router-link :to="{ name: 'contact' }" class="footer-link">Liên hệ</router-link>
            </li>
          </ul>
        </div>

        <!-- Cột 3: Chính sách -->
        <div class="col-lg-3 col-md-6">
          <h5 class="footer-heading">Chính sách</h5>
          <ul class="list-unstyled">
            <li><a href="#" class="footer-link">Chính sách bảo mật</a></li>
            <li><a href="#" class="footer-link">Chính sách đổi trả</a></li>
            <li><a href="#" class="footer-link">Hướng dẫn mua hàng</a></li>
            <li><a href="#" class="footer-link">Điều khoản dịch vụ</a></li>
          </ul>
        </div>

        <!-- Cột 4: Liên hệ -->
        <div class="col-lg-3 col-md-6">
          <h5 class="footer-heading">Liên hệ</h5>
          <ul class="list-unstyled footer-contact-info">
            <li>
              <img
                src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/39/Google_Maps_icon_%282015-2020%29.svg/1024px-Google_Maps_icon_%282015-2020%29.svg.png"
                alt="icon"
                class="me-2"
              />
              <a
                href="https://www.google.com/maps/@21.0358517,105.8583079,12z?entry=ttu"
                class="footer-link"
              >
                Số 123, Đường Vành Đai 1, Quận Hoàn Kiếm, Hà Nội
              </a>
            </li>
            <li>
              <img
                src="https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/WhatsApp.svg/1024px-WhatsApp.svg.png"
                alt="icon"
                class="me-2"
              />
              <a href="tel:0987654321" class="footer-link">0987 654 321</a>
            </li>
            <li>
              <img
                src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Gmail_icon_%282020%29.svg/2560px-Gmail_icon_%282020%29.svg.png"
                alt="icon"
                class="me-2"
              />
              <a href="mailto:info@tuonggo.pt" class="footer-link">info@tuonggo.pt</a>
            </li>
            <li class="mt-2">
              <a href="#" class="footer-social-icon me-2"><i class="bi bi-facebook"></i></a>
              <a href="#" class="footer-social-icon me-2"><i class="bi bi-youtube"></i></a>
              <a href="#" class="footer-social-icon"><i class="bi bi-tiktok"></i></a>
            </li>
          </ul>
        </div>
      </div>

      <hr class="my-4 text-white-50"/>

      <div class="d-flex justify-content-between align-items-center flex-wrap">
        <div class="text-white-50 small mb-2 mb-md-0">
          © {{ currentYear }} Tượng Gỗ Phong Thủy HV. All rights reserved.
        </div>
        <!-- Nút chuyển đổi theme -->
        <div>
          <button
            @click="uiStore.toggleTheme"
            class="btn btn-sm btn-outline-light theme-toggle-button"
            :title="uiStore.isDarkMode ? 'Chuyển sang giao diện Sáng' : 'Chuyển sang giao diện Tối'"
          >
            <i :class="uiStore.isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
            <span class="d-none d-sm-inline ms-1">
              {{ uiStore.isDarkMode ? 'Sáng' : 'Tối' }}
            </span>
          </button>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import {useUiStore} from '@/store/ui';

const currentYear = new Date().getFullYear();
const uiStore = useUiStore();
</script>

<style scoped>
.app-footer {
  background-color: #1c1f23;
  font-size: 0.9em;
  letter-spacing: 0.2px;
  transition: margin-left 0.3s ease;
}

.footer-heading {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 1rem;
  color: #ffffff;
}

.footer-link {
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  display: inline-block;
  margin-bottom: 0.5rem;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: #fff;
}

.footer-contact-info li {
  margin-bottom: 0.6rem;
  display: flex;
  align-items: center;
}

.footer-contact-info img {
  width: 18px;
  height: 18px;
  object-fit: contain;
  vertical-align: middle;
}

.footer-social-icon {
  color: rgba(255, 255, 255, 0.75);
  font-size: 1.3em;
  transition: color 0.3s ease;
}

.footer-social-icon:hover {
  color: #ffc107;
}

.theme-toggle-button {
  font-size: 0.8rem;
  padding: 0.3rem 0.6rem;
  display: inline-flex;
  align-items: center;
}

.theme-toggle-button i {
  vertical-align: middle;
  margin-bottom: 1px;
}

@media (max-width: 767.98px) {
  .app-footer h5 {
    margin-top: 1.5rem;
  }

  .app-footer .col-lg-4:first-child h5 {
    margin-top: 0;
  }
}
</style>
