// src/store/ui.js
import { defineStore } from 'pinia';

/**
 * Xác định theme ban đầu dựa trên:
 * 1. Giá trị đã lưu trong localStorage.
 * 2. Cài đặt ưa thích của hệ điều hành/trình duyệt.
 * 3. Mặc định là 'light'.
 */
const getInitialTheme = () => {
  // Ưu tiên 1: Lấy từ localStorage
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme === 'light' || savedTheme === 'dark') {
    console.log('UI Store: Found theme in localStorage:', savedTheme);
    return savedTheme;
  }

  // Ưu tiên 2: Kiểm tra cài đặt hệ thống/trình duyệt
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    console.log('UI Store: prefers-color-scheme is dark.');
    return 'dark';
  }

  // Mặc định: Theme sáng
  console.log('UI Store: Defaulting to light theme.');
  return 'light';
};

export const useUiStore = defineStore('ui', {
  state: () => ({
    // Khởi tạo theme dựa trên hàm getInitialTheme
    theme: getInitialTheme(), // Giá trị ban đầu sẽ là 'light' hoặc 'dark'
  }),
  getters: {
    /**
     * Getter kiểm tra xem theme hiện tại có phải là dark mode không.
     * @returns {boolean} True nếu là dark mode, false nếu là light mode.
     */
    isDarkMode: (state) => state.theme === 'dark',
  },
  actions: {
    /**
     * Đặt theme mới và áp dụng vào localStorage và class trên HTML.
     * @param {'light' | 'dark'} newTheme - Theme mới cần đặt.
     */
    setTheme(newTheme) {
      if (newTheme === 'light' || newTheme === 'dark') {
        this.theme = newTheme; // Cập nhật state
        localStorage.setItem('theme', newTheme); // Lưu vào localStorage

        // Áp dụng class vào thẻ <html> (hoặc <body> nếu bạn thích)
        // Xóa class cũ trước khi thêm class mới để đảm bảo chỉ có 1 class theme
        document.documentElement.classList.remove('light', 'dark');
        document.documentElement.classList.add(newTheme);

        console.log(`UI Store: Theme set to '${newTheme}' and applied to <html> tag.`);
      } else {
        console.warn(`UI Store: Invalid theme value provided: ${newTheme}`);
      }
    },

    /**
     * Chuyển đổi giữa theme sáng và tối.
     */
    toggleTheme() {
      const newTheme = this.theme === 'light' ? 'dark' : 'light';
      this.setTheme(newTheme);
    },

    /**
     * Khởi tạo và áp dụng theme khi ứng dụng bắt đầu.
     * Rất quan trọng để đảm bảo theme đúng được áp dụng ngay khi tải trang.
     */
    initializeTheme() {
      console.log('UI Store: Initializing theme...');
      // Gọi setTheme với giá trị theme hiện tại trong state
      // để đảm bảo class đúng được thêm vào thẻ <html>
      this.setTheme(this.theme);
    }
  },
});
