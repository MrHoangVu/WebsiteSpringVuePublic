package com.example.demo.repository;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Cho delete

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm item cụ thể trong cart theo sản phẩm
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // Xóa tất cả item của một cart (dùng khi clear cart)
    @Transactional // Cần transaction cho thao tác delete
    void deleteByCart(Cart cart);
}