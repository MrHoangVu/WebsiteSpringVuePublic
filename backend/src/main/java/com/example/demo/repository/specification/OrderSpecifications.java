// src/main/java/com/example/demo/repository/specification/OrderSpecifications.java
package com.example.demo.repository.specification;

import com.example.demo.entity.Order;
import com.example.demo.entity.User; // Import User để join
import jakarta.persistence.criteria.*; // Import Criteria API classes
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    public static Specification<Order> filterByAdminCriteria(
            String keyword, String status, LocalDate startDate, LocalDate endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Join với User để tìm kiếm theo thông tin user
            Join<Order, User> userJoin = root.join("user", JoinType.LEFT);

            // 1. Lọc theo Keyword
            if (StringUtils.hasText(keyword)) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                // Tìm theo ID đơn hàng (nếu keyword là số)
                Predicate idPredicate = null;
                try {
                    Integer orderId = Integer.parseInt(keyword);
                    idPredicate = criteriaBuilder.equal(root.get("id"), orderId);
                } catch (NumberFormatException e) {
                    // Keyword không phải số, bỏ qua tìm theo ID
                }

                // Tìm theo thông tin text
                Predicate userUsernameLike = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("username")), keywordLower);
                Predicate userFullNameLike = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("fullName")), keywordLower);
                Predicate guestEmailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("guestEmail")), keywordLower);
                Predicate recipientNameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("shippingRecipientName")), keywordLower);
                Predicate recipientPhoneLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("shippingRecipientPhone")), keywordLower); // Giả sử phone là string

                Predicate textSearchPredicate = criteriaBuilder.or(
                        userUsernameLike, userFullNameLike, guestEmailLike, recipientNameLike, recipientPhoneLike
                );

                // Kết hợp tìm theo ID (nếu có) và tìm theo text
                if (idPredicate != null) {
                    predicates.add(criteriaBuilder.or(idPredicate, textSearchPredicate));
                } else {
                    predicates.add(textSearchPredicate);
                }
            }

            // 2. Lọc theo Status
            if (StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("status")), status.toUpperCase()));
            }

            // 3. Lọc theo Ngày đặt hàng (Date Range)
            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDateTime));
            }
            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDateTime));
            }


            // Kết hợp các điều kiện bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}