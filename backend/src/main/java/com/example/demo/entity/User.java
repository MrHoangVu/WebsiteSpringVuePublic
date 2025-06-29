// src/main/java/com/example/demo/entity/User.java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "role", nullable = false, length = 50)
    private String role; // ADMIN, CUSTOMER

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- CÁC TRƯỜNG MỚI CHO QUẢN LÝ KHÁCH HÀNG ---
    @Column(name = "total_spent", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO; // Tổng chi tiêu

    @Column(name = "tier", nullable = false, length = 20)
    private String tier = "BRONZE"; // Bậc khách hàng (BRONZE, SILVER, GOLD, DIAMOND)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<ProductReview> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<Article> articles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Address> addresses;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Order> orders;

}