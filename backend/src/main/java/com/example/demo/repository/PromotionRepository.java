// src/main/java/com/example/demo/repository/PromotionRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer>, JpaSpecificationExecutor<Promotion> { // <<< THÊM KẾ THỪA

    Optional<Promotion> findByCodeIgnoreCase(String code);

}