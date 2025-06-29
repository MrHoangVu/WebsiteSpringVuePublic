// src/http/chatService.js
import apiClient from '../axios.js';

/**
 * Gửi tin nhắn đến backend để lấy phản hồi từ ai.
 * @param {string} message Tin nhắn của người dùng.
 * @param {Array<object>} history Lịch sử chat gần đây (mảng các object { role: 'user'/'model', parts: [{text: '...'}] }).
 * @returns Promise chứa response dạng { reply: string }
 */
export const sendMessageToGemini = (message, history = []) => {
  if (!message || message.trim() === '') {
    return Promise.reject(new Error("Message cannot be empty"));
  }
  // Đảm bảo history có cấu trúc đúng mà backend mong đợi
  const validHistory = history.filter(item =>
    item && typeof item === 'object' &&
    (item.role === 'user' || item.role === 'model') &&
    Array.isArray(item.parts) && item.parts.length > 0 &&
    typeof item.parts[0].text === 'string'
  );

  return apiClient.post('/chat/gemini', { message, history: validHistory });
};
