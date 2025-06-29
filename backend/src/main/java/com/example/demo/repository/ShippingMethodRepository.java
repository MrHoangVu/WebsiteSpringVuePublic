package com.example.demo.repository;

import com.example.demo.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Integer> {
    // Lấy tất cả các phương thức đang hoạt động
    List<ShippingMethod> findByIsActiveTrue();
    Optional<ShippingMethod> findByNameIgnoreCase(String name);
}