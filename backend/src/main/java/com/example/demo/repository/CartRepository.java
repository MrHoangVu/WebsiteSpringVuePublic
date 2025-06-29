package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Integer userId);
    Optional<Cart> findBySessionId(String sessionId);
    // Optional: Tìm cart của user hoặc session (ưu tiên user nếu có)
    default Optional<Cart> findActiveCart(Integer userId, String sessionId) {
        if (userId != null) {
            return findByUserId(userId);
        } else if (sessionId != null) {
            return findBySessionId(sessionId);
        }
        return Optional.empty();
    }
}