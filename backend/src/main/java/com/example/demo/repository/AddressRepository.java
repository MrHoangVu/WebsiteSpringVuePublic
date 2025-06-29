package com.example.demo.repository;

import com.example.demo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // Lấy tất cả địa chỉ của một user
    List<Address> findByUserId(Integer userId);

    // Lấy địa chỉ mặc định (giao hàng hoặc thanh toán)
    Optional<Address> findByUserIdAndIsDefaultShippingTrue(Integer userId);
    Optional<Address> findByUserIdAndIsDefaultBillingTrue(Integer userId);
}