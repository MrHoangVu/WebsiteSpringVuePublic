package com.example.demo.service;

import com.example.demo.dto.cart.AddItemRequestDTO;
import com.example.demo.dto.cart.CartDTO;
import com.example.demo.entity.Cart; // Import entity Cart nếu cần trả về entity

import java.math.BigDecimal;

public interface CartService {
    Cart getOrCreateCart(String username, String cartSessionId);
    CartDTO getCartDto(String username, String cartSessionId);
    CartDTO addItemToCart(String username, String cartSessionId, AddItemRequestDTO request);
    CartDTO updateItemQuantity(String username, String cartSessionId, Integer productId, int newQuantity);
    CartDTO removeItemFromCart(String username, String cartSessionId, Integer productId);
    CartDTO clearCart(String username, String cartSessionId);
    CartDTO mergeGuestCartToUserCart(String guestCartSessionId, String username);
    BigDecimal calculateSubtotalForCart(String username, String cartSessionId); // Thêm hàm này nếu CheckoutService cần
}