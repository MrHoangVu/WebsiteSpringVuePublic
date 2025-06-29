package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Carts")
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // User có thể null cho guest
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 255) // Session ID cho guest
    private String sessionId;

    // CascadeType.ALL: Lưu/Xóa Cart thì lưu/xóa CartItem
    // orphanRemoval=true: Xóa CartItem khỏi Set này thì xóa luôn trong DB
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method để thêm item, đảm bảo tính nhất quán 2 chiều
    public void addCartItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    // Helper method để xóa item
    public void removeCartItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null); // Hoặc không cần nếu dùng orphanRemoval
    }
}