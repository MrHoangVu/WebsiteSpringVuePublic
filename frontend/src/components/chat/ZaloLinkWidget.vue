<template>
  <a
    :href="zaloLink"
    target="_blank"
    rel="noopener noreferrer"
    class="zalo-link-widget btn rounded-circle shadow-lg"
  aria-label="Liên hệ qua Zalo"
  title="Chat với chúng tôi qua Zalo"
  >
  <img :src="zaloImageUrl" alt="Zalo Icon" class="zalo-icon-img" />
  </a>
</template>

<script setup>
import zaloIconPath from '@/assets/images/zalo-icon.png';
const zaloImageUrl = zaloIconPath;

const zaloLink = 'https://id.zalo.me/account?continue=http%3A%2F%2Fzalo.me%2Fg%2Fnbwawu184';

</script>

<style scoped>
.zalo-link-widget {
  position: fixed;
  bottom: 95px;
  right: 20px;
  z-index: 1048;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center; /* Căn giữa ảnh theo chiều ngang */
  padding: 5px; /* Thêm chút padding để ảnh không chạm viền */
  background-color: white; /* Nền trắng hoặc trong suốt tùy ý */
  border: 1px solid #ccc; /* Có thể thêm viền nhẹ */
  box-sizing: border-box; /* Đảm bảo padding không làm tăng kích thước */
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;

}

.zalo-link-widget:hover {
  transform: scale(1.1);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.25);
}

.zalo-icon-img {
  display: block; /* Đảm bảo ảnh là block element */
  width: 100%; /* Chiếm toàn bộ chiều rộng của thẻ a (sau padding) */
  height: 100%; /* Chiếm toàn bộ chiều cao */
  object-fit: contain; /* Đảm bảo ảnh vừa vặn bên trong, giữ tỉ lệ */
  border-radius: 50%; /* Để tạo hình tròn */
}

/* Điều chỉnh vị trí trên màn hình nhỏ nếu cần */
@media (max-width: 767px) {
  .zalo-link-widget {
    bottom: 85px;
    right: 15px;
    width: 45px;
    height: 45px;
    padding: 4px;
  }
}
</style>
