package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- THÊM MỚI ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // --- THÊM MỚI ---
    @Column(name = "guest_email", length = 255)
    private String guestEmail;

    @Column(name = "customer_name", length = 150)
    private String customerName;
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    @Column(name = "customer_address", length = 500)
    private String customerAddress;

    // --- THÊM MỚI (Copy từ Address hoặc Guest nhập) ---
    @Column(name = "shipping_recipient_name", length = 150)
    private String shippingRecipientName;
    @Column(name = "shipping_recipient_phone", length = 20)
    private String shippingRecipientPhone;
    @Column(name = "shipping_street_address", length = 500)
    private String shippingStreetAddress;
    @Column(name = "shipping_ward", length = 100)
    private String shippingWard;
    @Column(name = "shipping_district", length = 100)
    private String shippingDistrict;
    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    // --- THÊM MỚI (Billing nếu cần, tương tự shipping) ---
    // @Column(name = "billing_recipient_name", length = 150) ...

    // --- THÊM MỚI (ID địa chỉ đã lưu) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id") // Khớp FK
    private Address shippingAddress; // Dùng obj Address thay vì chỉ ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id") // Khớp FK
    private Address billingAddress; // Dùng obj Address thay vì chỉ ID

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount; // Đây có thể là tổng tiền hàng + phí VC - giảm giá

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "order_date", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderItem> orderItems;

    // --- ĐÃ THÊM TRƯỚC ĐÓ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    // --- ĐÃ THÊM TRƯỚC ĐÓ ---
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // --- THÊM MỚI ---
    @ManyToOne(fetch = FetchType.LAZY) // shipping_method_id cho phép NULL
    @JoinColumn(name = "shipping_method_id") // Khớp FK
    private ShippingMethod shippingMethod;

    // --- THÊM MỚI ---
    @Column(name = "shipping_cost", nullable = false, precision = 18, scale = 2) // Khớp cột
    private BigDecimal shippingCost = BigDecimal.ZERO; // Giá trị mặc định

    // --- THÊM MỚI ---
    @Column(name = "payment_method", length = 50) // Khớp cột
    private String paymentMethod; // COD, BANK_TRANSFER, VNPAY...

    // --- THÊM MỚI (Nếu có bảng PaymentTransactions) ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentTransaction> paymentTransactions;
    // === THÊM TRƯỜNG MỚI ===
    @Column(name = "order_note", length = 500) // Khớp với migration
    private String orderNote;
    // ========================
}
