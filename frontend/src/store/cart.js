import { defineStore } from "pinia";
import {
  getCart,
  addItemToCart,
  updateCartItemQuantity,
  removeCartItem,
  clearServerCart,
  mergeGuestCart,
} from "@/http/modules/public/cartService.js";
import { useAuthStore } from "./auth";
import { getCartSessionId, clearCartSessionId } from "@/utils/cartSession";

export const useCartStore = defineStore("cart", {
  state: () => ({
    items: [],
    cartId: null,
    isLoading: false,
    error: null, // Chỉ lưu message lỗi dạng string
    // Thêm cờ để biết đã fetch lần đầu chưa
    // initialFetchDone: false, // <<< Bỏ cờ này, dựa vào cartId hoặc length để fetch lại
  }),

  getters: {
    totalUniqueItems: (state) => state.items.length,
    totalItemsCount: (state) => state.items.reduce((sum, item) => sum + (item.quantity || 0), 0),
    subtotal: (state) =>
      state.items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0),
    findItem: (state) => (productId) => state.items.find((item) => item.productId === productId),
    // Getter kiểm tra giỏ hàng có trống không (bao gồm cả khi đang load lần đầu)
    isEmpty: (state) => state.items.length === 0 && !state.isLoading,
    // Getter kiểm tra giỏ hàng có thực sự trống (không đang load, không lỗi, và length = 0)
    isTrulyEmpty: (state) => state.items.length === 0 && !state.isLoading && !state.error,
  },

  actions: {
    // --- Core Actions ---
    async fetchCart() {
      // Chỉ fetch nếu chưa có cartId hoặc chưa có item (trừ khi đang load)
      // Điều này tránh fetch lại không cần thiết nếu cart đã có
      if (this.isLoading) return; // Đang load thì thôi
      // if (this.cartId && this.items.length > 0) {
      //   console.log('CartStore: Cart already loaded, skipping fetch.');
      //   return; // Đã có cart rồi thì thôi
      // }

      this.isLoading = true;
      this.error = null;
      try {
        console.log("CartStore: Fetching cart...");
        const response = await getCart(); // Service tự gửi token/session
        this.items = response.data?.items || [];
        this.cartId = response.data?.cartId || null;
        console.log("CartStore: Cart fetched successfully.", {
          cartId: this.cartId,
          itemsCount: this.items.length,
        });
      } catch (err) {
        console.error("CartStore: Error fetching cart:", err);
        // Chỉ set lỗi nếu thực sự là lỗi API, không phải lỗi do user (ví dụ 404 nếu cart trống?)
        // Backend nên trả về cart rỗng thay vì 404
        if (err.response?.status !== 404) {
          // Giả sử 404 là cart trống hoặc không tồn tại (cho guest mới)
          this.error = err.response?.data?.message || err.message || "Không thể tải giỏ hàng.";
        }
        // Dù lỗi hay 404, vẫn reset state về rỗng
        this.items = [];
        this.cartId = null;
      } finally {
        this.isLoading = false;
      }
    },

    async addItem(productId, quantity = 1) {
      if (!productId || quantity <= 0) return Promise.reject(new Error("Dữ liệu không hợp lệ")); // Nên reject Promise
      this.isLoading = true; // Set loading toàn cục hoặc loading cho item cụ thể
      this.error = null; // Clear lỗi cũ trước khi thử lại
      console.log(`CartStore: Adding item ${productId} quantity ${quantity}...`);
      try {
        const response = await addItemToCart({ productId, quantity });
        this.items = response.data?.items || [];
        this.cartId = response.data?.cartId || null;
        console.log("CartStore: Item added/updated.", {
          cartId: this.cartId,
          itemsCount: this.items.length,
        });
      } catch (err) {
        console.error("CartStore: Error adding item:", err);
        this.error = err.response?.data?.message || "Lỗi khi thêm sản phẩm vào giỏ.";
        throw err; // Ném lỗi ra để component xử lý
      } finally {
        this.isLoading = false;
      }
    },

    async updateQuantity(productId, quantity) {
      if (!productId) return Promise.reject(new Error("Thiếu ID sản phẩm"));
      // Xử lý quantity <= 0 bằng cách gọi removeItem
      if (quantity <= 0) {
        console.log(`CartStore: Quantity <= 0 for ${productId}, removing item.`);
        return this.removeItem(productId);
      }

      this.isLoading = true;
      this.error = null;
      console.log(`CartStore: Updating item ${productId} quantity to ${quantity}...`);
      try {
        const response = await updateCartItemQuantity(productId, quantity);
        this.items = response.data?.items || [];
        this.cartId = response.data?.cartId || null;
        console.log("CartStore: Quantity updated.", {
          cartId: this.cartId,
          itemsCount: this.items.length,
        });
      } catch (err) {
        console.error("CartStore: Error updating quantity:", err);
        this.error = err.response?.data?.message || "Lỗi khi cập nhật số lượng.";
        // Không ném lỗi ra vì có thể chỉ là lỗi tạm thời, không muốn dừng hẳn flow
        // Nếu lỗi, fetch lại toàn bộ giỏ hàng để đảm bảo đồng bộ
        await this.fetchCart();
        // throw err; // Chỉ throw nếu muốn component xử lý lỗi này
      } finally {
        this.isLoading = false;
      }
    },

    async removeItem(productId) {
      if (!productId) return Promise.reject(new Error("Thiếu ID sản phẩm"));
      this.isLoading = true;
      this.error = null;
      console.log(`CartStore: Removing item ${productId}...`);
      try {
        const response = await removeCartItem(productId);
        this.items = response.data?.items || [];
        this.cartId = response.data?.cartId || null;
        console.log("CartStore: Item removed.", {
          cartId: this.cartId,
          itemsCount: this.items.length,
        });
      } catch (err) {
        console.error("CartStore: Error removing item:", err);
        this.error = err.response?.data?.message || "Lỗi khi xóa sản phẩm khỏi giỏ.";
        // Fetch lại giỏ hàng nếu xóa lỗi
        await this.fetchCart();
        // throw err; // Chỉ throw nếu muốn component xử lý
      } finally {
        this.isLoading = false;
      }
    },

    // Xóa state ở client
    clearClientCart() {
      this.items = [];
      this.cartId = null;
      this.error = null;
      console.log("CartStore: Cleared client cart state.");
    },

    // Gọi API xóa cart trên server và xóa state client
    async clearCart() {
      // Chỉ gọi API nếu có cartId (có giỏ hàng trên server)
      if (!this.cartId && this.items.length === 0) {
        console.log("CartStore: Cart already empty or no server cart, clearing client only.");
        this.clearClientCart();
        return;
      }

      this.isLoading = true;
      this.error = null;
      console.log("CartStore: Clearing server cart...");
      try {
        // Chỉ gọi API nếu thực sự có cartId
        if (this.cartId) {
          await clearServerCart();
          console.log("CartStore: Server cart cleared.");
        } else {
          console.log("CartStore: No cartId found, skipping server clear.");
        }
        this.clearClientCart(); // Luôn clear client state
      } catch (err) {
        console.error("CartStore: Error clearing server cart:", err);
        this.error = "Lỗi khi xóa giỏ hàng trên máy chủ.";
        // Không nên throw lỗi này ra, chỉ cần báo lỗi và giữ nguyên state hiện tại (có thể đã bị xóa ở client)
      } finally {
        this.isLoading = false;
      }
    },

    // --- Login/Logout Handling (Giữ nguyên) ---
    async handleLoginMerge() {
      // Logic merge giữ nguyên
      const authStore = useAuthStore();
      if (!authStore.isAuthenticated) return;
      const guestSessionId = getCartSessionId();
      if (!guestSessionId) {
        console.log("CartStore: No guest session ID. Fetching user cart.");
        await this.fetchCart();
        return;
      }
      console.log(`CartStore: Merging guest cart (Session: ${guestSessionId})...`);
      this.isLoading = true;
      this.error = null;
      try {
        const response = await mergeGuestCart(guestSessionId);
        this.items = response.data?.items || [];
        this.cartId = response.data?.cartId || null;
        console.log("CartStore: Guest cart merged successfully.", {
          cartId: this.cartId,
          itemsCount: this.items.length,
        });
        clearCartSessionId();
      } catch (err) {
        console.error("CartStore: Error merging guest cart:", err);
        this.error = "Lỗi khi gộp giỏ hàng từ phiên khách.";
        // Dù lỗi merge, vẫn fetch cart của user
        await this.fetchCart();
      } finally {
        this.isLoading = false;
      }
    },

    handleLogoutCleanup() {
      // Logic cleanup giữ nguyên
      this.clearClientCart();
      console.log("CartStore: Cleaned up cart state on logout.");
    },
  },
});
