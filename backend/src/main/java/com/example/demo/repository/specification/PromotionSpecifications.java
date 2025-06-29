// src/main/java/com/example/demo/repository/specification/PromotionSpecifications.java
package com.example.demo.repository.specification; // Hoặc package phù hợp

import com.example.demo.entity.Promotion;
import jakarta.persistence.criteria.Predicate; // Import Predicate
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils; // Import StringUtils

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; // Import List

public class PromotionSpecifications {

    public static Specification<Promotion> filterByAdminCriteria(
            String statusFilter, String targetTier) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            // 1. Lọc theo Status Filter
            if (StringUtils.hasText(statusFilter)) {
                switch (statusFilter.toUpperCase()) {
                    case "ACTIVE": // Đang diễn ra
                        predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), now));
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now));
                        break;
                    case "UPCOMING": // Sắp diễn ra
                        predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
                        predicates.add(criteriaBuilder.greaterThan(root.get("startDate"), now));
                        break;
                    case "EXPIRED": // Đã hết hạn (cả active và inactive)
                        predicates.add(criteriaBuilder.lessThan(root.get("endDate"), now));
                        break;
                    case "INACTIVE": // Ngừng hoạt động
                        predicates.add(criteriaBuilder.isFalse(root.get("isActive")));
                        break;
                }
            }

            // 2. Lọc theo Target Tier
            if (StringUtils.hasText(targetTier)) {
                // Tìm các promotion có targetTiers chứa tier được chỉ định
                // Hoặc tìm các promotion không có targetTiers (áp dụng cho tất cả)
                Predicate tierContains = criteriaBuilder.like(root.get("targetTiers"), "%" + targetTier.toUpperCase() + "%");
                Predicate tierIsNull = criteriaBuilder.isNull(root.get("targetTiers"));
                // Nếu muốn chỉ lấy KM cho tier đó VÀ KM chung:
                // predicates.add(criteriaBuilder.or(tierContains, tierIsNull));

                // Nếu muốn chỉ lấy KM cho CHÍNH XÁC tier đó (không bao gồm KM chung):
                predicates.add(tierContains);
                // Chọn 1 trong 2 logic trên tùy yêu cầu nghiệp vụ
            }

            // Kết hợp các điều kiện bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Có thể thêm các Specification khác nếu cần
}