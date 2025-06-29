<template>
  <div class="container text-center mt-5">
    <h1>404 - Không Tìm Thấy Trang</h1>
    <p>Trang bạn đang tìm kiếm không tồn tại.</p>
    <router-link :to="{ name: 'home' }" class="btn btn-primary">Về Trang Chủ</router-link>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>
