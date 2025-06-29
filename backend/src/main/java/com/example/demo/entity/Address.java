package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set; // Nếu cần OneToMany đến Orders

@Entity
@Table(name = "Addresses")
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "street_address", nullable = false, length = 500)
    private String streetAddress;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "country", nullable = false, length = 10)
    private String country = "VN";

    @Column(name = "is_default_shipping", nullable = false)
    private Boolean isDefaultShipping = false;

    @Column(name = "is_default_billing", nullable = false)
    private Boolean isDefaultBilling = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    //  Quan hệ ngược lại tới Orders nếu muốn lấy Orders theo Address
    // @OneToMany(mappedBy = "shippingAddress") // Hoặc billingAddress
    // private Set<Order> ordersShippedHere;
}