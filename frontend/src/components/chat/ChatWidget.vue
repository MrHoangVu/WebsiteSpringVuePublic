<template>
  <div class="chat-widget-container">
    <!-- Floating Action Button (FAB) -->
    <!-- btn-primary sẽ tự đổi màu -->
    <button
      class="btn btn-primary rounded-circle shadow-lg chat-fab"
      type="button"
      @click="toggleChat"
      aria-label="Mở cửa sổ chat"
      title="Chat với trợ lý ảo"
      :aria-expanded="isChatOpen.toString()"
    >
      <i v-if="!isChatOpen" class="bi bi-chat-dots-fill fs-4"></i>
      <i v-else class="bi bi-x-lg fs-4"></i>
    </button>

    <!-- Chat Window -->
    <div
      v-if="isChatVisible"
      class="chat-window card shadow-lg"
      :class="{ 'open': isChatOpen }"
      ref="chatWindowRef"
      role="dialog"
      aria-modal="true"
      aria-labelledby="chatWindowTitle"
    >
      <!-- Header -->
      <!-- Giữ bg-light và border-bottom, chúng sẽ dùng biến Bootstrap -->
      <div
        class="card-header chat-header bg-light border-bottom d-flex justify-content-between align-items-center">
        <h5 class="card-title chat-title mb-0 fs-6 fw-semibold" id="chatWindowTitle">
          <i class="bi bi-robot me-2"></i> Trợ lý ảo Phong Thủy HV
        </h5>
        <button type="button" class="btn-close small chat-close-btn" @click="toggleChat"
                aria-label="Đóng"></button>
      </div>

      <!-- Body -->
      <div class="card-body chat-body d-flex flex-column p-0">

        <!-- Message Area -->
        <div class="message-area flex-grow-1 p-3" ref="messageAreaRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="chat-message mb-2"
            :class="getMessageClass(msg.role)"
          >
            <div class="message-bubble" v-html="formatMessageContent(msg)"></div>
          </div>
          <!-- Loading Indicator -->
          <div v-if="isLoading" class="chat-message bot mb-2">
            <div class="message-bubble typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
          <!-- Error Message -->
          <!-- bg-danger-subtle, text-danger, border-danger-subtle sẽ tự đổi màu -->
          <div v-if="error" class="chat-message bot mb-2">
            <div class="message-bubble bg-danger-subtle text-danger border border-danger-subtle">
              <i class="bi bi-exclamation-circle-fill me-1"></i> {{ error }}
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <!-- Giữ bg-light và border-top, chúng sẽ dùng biến Bootstrap -->
        <div class="input-area p-3 border-top bg-light">
          <form @submit.prevent="sendMessage" class="input-form">
            <div class="input-group">
              <input
                type="text"
                class="form-control"
                v-model.trim="newMessage"
                placeholder="Nhập câu hỏi của bạn..."
                aria-label="Nhập câu hỏi"
                :disabled="isLoading"
                ref="messageInputRef"
              />
              <!-- btn-primary sẽ tự đổi màu -->
              <button class="btn btn-primary" type="submit" :disabled="isLoading || !newMessage">
                <!-- spinner-border sẽ tự đổi màu -->
                <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status"
                      aria-hidden="true"></span>
                <i v-else class="bi bi-send-fill"></i>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onBeforeUnmount, nextTick} from 'vue';
import {sendMessageToGemini} from '@/http/chatbox/chatService.js';
import DOMPurify from 'dompurify';
import {marked} from 'marked';

const isChatOpen = ref(false);
const isChatVisible = ref(false);
const messages = ref([]);
const newMessage = ref('');
const isLoading = ref(false);
const error = ref(null);
const chatWindowRef = ref(null);
const messageAreaRef = ref(null);
const messageInputRef = ref(null);

const toggleChat = () => {
  if (isChatOpen.value) {
    isChatOpen.value = false;
    setTimeout(() => {
      isChatVisible.value = false;
    }, 300);
  } else {
    isChatVisible.value = true;
    nextTick(() => {
      isChatOpen.value = true;
      messageInputRef.value?.focus();
      scrollToBottom();
    });
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    const el = messageAreaRef.value;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  });
};

const getMessageClass = (role) => {
  return role === 'user' ? 'user' : 'bot';
};

const formatMessageContent = (message) => {
  const text = message?.parts?.[0]?.text;
  if (!text) return '';
  try {
    const cleanHtml = DOMPurify.sanitize(marked.parse(text), {USE_PROFILES: {html: true}});
    return cleanHtml;
  } catch (e) {
    console.error("Error formatting message:", e);
    return text;
  }
};

const sendMessage = async () => {
  if (!newMessage.value || isLoading.value) return;

  const userMsgText = newMessage.value;
  messages.value.push({role: 'user', parts: [{text: userMsgText}]});
  newMessage.value = '';
  isLoading.value = true;
  error.value = null;
  scrollToBottom();

  const historyLimit = 18;
  const historyToSend = messages.value.slice(Math.max(messages.value.length - 1 - historyLimit, 0), -1)
    .map(msg => ({
      role: msg.role,
      content: msg.parts[0].text
    }));

  try {
    const response = await sendMessageToGemini(userMsgText, historyToSend);
    if (response && response.data && response.data.reply) {
      messages.value.push({role: 'model', parts: [{text: response.data.reply}]});
    } else {
      console.warn("Invalid response structure from backend (expected response.data.reply):", response);
      error.value = "Phản hồi từ trợ lý ảo không hợp lệ hoặc thiếu nội dung.";
    }
  } catch (err) {
    console.error("Error sending message:", err);
    const errorMsg = err.response?.data?.reply || err.response?.data?.error || err.response?.data?.message || err.message || "Không thể gửi tin nhắn.";
    error.value = errorMsg;
  } finally {
    isLoading.value = false;
    scrollToBottom();
    nextTick(() => {
      messageInputRef.value?.focus();
    });
  }
};

const handleClickOutside = (event) => {
  if (isChatOpen.value && chatWindowRef.value && !chatWindowRef.value.contains(event.target)) {
    const fabButton = event.target.closest('.chat-fab');
    if (!fabButton) {
      toggleChat();
    }
  }
};

onMounted(() => {
  messages.value.push({
    role: 'model',
    parts: [{text: 'Xin chào! Tôi có thể giúp gì cho bạn về Tượng Gỗ Phong Thủy HV?'}]
  });
  document.addEventListener('mousedown', handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside);
});

</script>

<style scoped>
.chat-widget-container {
}

.chat-fab {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1051;
  width: 60px;
  height: 60px;
  font-size: 1.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
  /* Màu nền/chữ được xử lý bởi class btn-primary */
}

.chat-fab:hover {
  transform: scale(1.1);
}

.chat-window {
  position: fixed;
  bottom: 90px;
  right: 20px;
  width: 370px;
  height: 70vh;
  max-height: 550px;
  z-index: 1050;
  background-color: var(--card-bg); /* <<< Sử dụng biến */
  border: 1px solid var(--card-border); /* <<< Sử dụng biến */
  border-radius: 0.75rem;
  box-shadow: var(--bs-box-shadow-lg); /* <<< Sử dụng biến shadow Bootstrap */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  opacity: 0;
  transform: translateY(30px) scale(0.95);
  transform-origin: bottom right;
  visibility: hidden;
  transition: opacity 0.3s ease-out, transform 0.3s ease-out, visibility 0.3s;
}

.chat-window.open {
  opacity: 1;
  transform: translateY(0) scale(1);
  visibility: visible;
}

/* Header */
.chat-header {
  /* Màu nền và viền được xử lý bởi class bg-light và border-bottom trong template */
  padding: 0.8rem 1.2rem;
}

.chat-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary); /* <<< Sử dụng biến */
}

.chat-close-btn {
  font-size: 0.75rem;
  /* Màu của btn-close được Bootstrap xử lý qua biến */
}

/* Body */
.chat-body {
  display: flex;
  flex-direction: column;
  padding: 0;
  flex-grow: 1;
  min-height: 0;
}

/* Message Area */
.message-area {
  overflow-y: auto;
  scroll-behavior: smooth;
  background-color: var(--bg-primary); /* <<< Sử dụng biến */
  flex-grow: 1;
  min-height: 0;
}

/* Custom scrollbar */
.message-area::-webkit-scrollbar {
  width: 6px;
}

.message-area::-webkit-scrollbar-track {
  background: var(--bg-accent); /* <<< Sử dụng biến */
  border-radius: 3px;
}

.message-area::-webkit-scrollbar-thumb {
  background: var(--border-color); /* <<< Sử dụng biến */
  border-radius: 3px;
}

.message-area::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary); /* <<< Sử dụng biến */
}

.chat-message {
  display: flex;
  margin-bottom: 0.5rem;
}

.chat-message.user {
  justify-content: flex-end;
}

.chat-message.bot {
  justify-content: flex-start;
}

.message-bubble {
  padding: 0.7rem 1rem;
  border-radius: 1.1rem;
  max-width: 85%;
  word-wrap: break-word;
  font-size: 0.95rem;
  line-height: 1.5;
  box-shadow: var(--bs-box-shadow-sm); /* <<< Sử dụng biến shadow Bootstrap */
}

.message-bubble :deep(p:last-child) {
  margin-bottom: 0;
}

.message-bubble :deep(ul), .message-bubble :deep(ol) {
  padding-left: 1.2rem;
  margin-bottom: 0.5rem;
}

.chat-message.user .message-bubble {
  background-color: var(--bs-primary); /* <<< Sử dụng biến Bootstrap */
  color: var(--text-on-primary-bg); /* <<< Sử dụng biến */
  border-bottom-right-radius: 0.3rem;
}

.chat-message.bot .message-bubble {
  background-color: var(--bg-accent); /* <<< Sử dụng biến (nền khác biệt chút) */
  color: var(--text-primary); /* <<< Sử dụng biến */
  border: 1px solid var(--border-color-subtle); /* <<< Sử dụng biến */
  border-bottom-left-radius: 0.3rem;
}

/* Typing Indicator */
.typing-indicator span {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: var(--text-secondary); /* <<< Sử dụng biến */
  border-radius: 50%;
  margin: 0 2px;
  animation: typing 1.2s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%, 100% {
    opacity: 0.4;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Input Area */
.input-area {
  /* Màu nền và viền được xử lý bởi class bg-light và border-top trong template */
}

.input-form {
}

.input-area .form-control {
  border-radius: 1.5rem 0 0 1.5rem !important;
  padding: 0.6rem 1rem;
  font-size: 0.95rem;
  border-color: var(--border-color); /* <<< Sử dụng biến */
  background-color: var(--bs-body-bg); /* <<< Đảm bảo input có nền đúng */
  color: var(--bs-body-color); /* <<< Đảm bảo input có màu chữ đúng */
  box-shadow: none !important;
}

.input-area .form-control:focus {
  border-color: var(--bs-primary); /* <<< Giữ màu primary khi focus */
  /* Các thuộc tính focus khác (shadow, ...) được Bootstrap xử lý */
}

.input-area .btn {
  border-radius: 0 1.5rem 1.5rem 0 !important;
  padding: 0.6rem 1rem;
  box-shadow: none !important;
  /* Màu nền/chữ/hover được xử lý bởi class btn-primary */
}

.input-area .btn i {
  vertical-align: middle;
  font-size: 1.1rem;
}

</style>
