
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "products") // Khớp tên bảng
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255) // Khớp tên cột
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255) // Khớp tên cột
    private String slug;

    @Lob // Cho NVARCHAR(MAX)
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)") // Khớp tên cột và kiểu
    private String description;

    @Column(name = "price", nullable = false, precision = 18, scale = 2) // Khớp tên cột
    private BigDecimal price;

    @Column(name = "stock") // Mặc định là 0 từ DB, không cần nullable=false ở đây
    private Integer stock;

    @Column(name = "image_url", length = 512) // Khớp tên cột
    private String imageUrl;

    @Column(name = "dimensions", length = 100) // Khớp tên cột
    private String dimensions;

    @Column(name = "material", length = 100) // Khớp tên cột
    private String material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // Khớp tên cột FK
    private Category category;

    @Column(name = "created_at", nullable = false, updatable = false) // Khớp tên cột, nullable=false
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // Khớp tên cột, nullable=false
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- THÊM CỘT MỚI ---
    @Column(name = "is_active", nullable = false) // Khớp tên cột, nullable=false
    private Boolean isActive = true; // Giá trị mặc định trong Java (không bắt buộc nếu DB đã có DEFAULT)

    // Quan hệ với OrderItems (đã có)
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems;

    // --- THÊM QUAN HỆ MỚI ---
    // Một sản phẩm có thể có nhiều đánh giá
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY) // Cascade REMOVE để xóa review nếu xóa product
    private Set<ProductReview> reviews;

}