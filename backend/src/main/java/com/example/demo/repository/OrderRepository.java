// src/main/java/com/example/demo/repository/OrderRepository.java
package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< Import JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Order entities.
 * Includes methods for basic CRUD, custom queries for statistics,
 * user-specific order retrieval, and specification-based filtering for admins.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> { // <<< Kế thừa JpaSpecificationExecutor

    // ==================================
    // === CUSTOM QUERIES FOR STATS ===
    // ==================================

    /**
     * Counts the number of orders within a given date range.
     * @param startDate Start timestamp (inclusive).
     * @param endDate End timestamp (inclusive).
     * @return Total number of orders.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    long countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calculates the sum of total amounts for orders within a given date range.
     * Returns null if no orders are found.
     * @param startDate Start timestamp (inclusive).
     * @param endDate End timestamp (inclusive).
     * @return Sum of total amounts, or null.
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Finds products based on quantity sold within a date range.
     * Note: This might need optimization for large datasets. Consider dedicated reporting tables or views.
     * @param startDate Start timestamp (inclusive).
     * @param endDate End timestamp (inclusive).
     * @param pageable Limits the number of top products returned (e.g., PageRequest.of(0, 5)).
     * @return List of Object arrays, each containing [productName (String), totalQuantity (Long/Integer)].
     */
    @Query("SELECT oi.product.name, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
            "AND oi.product IS NOT NULL " + // Ensure product exists
            "GROUP BY oi.product.name " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Calculates daily revenue from COMPLETED orders within a date range.
     * @param startDateTime Start timestamp (inclusive).
     * @param endDateTime End timestamp (inclusive).
     * @return List of Object arrays, each containing [orderDay (java.sql.Date), dailyRevenue (BigDecimal)].
     */
    @Query("SELECT CAST(o.orderDate AS DATE) as orderDay, SUM(o.totalAmount) as dailyRevenue " +
            "FROM Order o " +
            "WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime AND o.status = 'COMPLETED' " + // Only completed orders
            "GROUP BY CAST(o.orderDate AS DATE) " +
            "ORDER BY orderDay ASC")
    List<Object[]> findDailyRevenueBetweenDates(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // ======================================
    // === METHODS FOR USER ORDER VIEWS ===
    // ======================================

    /**
     * Finds orders for a specific user, ordered by date descending.
     * Uses EntityGraph to eagerly fetch OrderItems and their associated Products
     * to avoid N+1 queries when displaying the order list.
     * @param userId ID of the user.
     * @param pageable Pagination and sorting information.
     * @return A Page of Orders.
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Page<Order> findByUserIdOrderByOrderDateDesc(Integer userId, Pageable pageable);

    /**
     * Finds a specific order by its ID and the user's ID.
     * Uses EntityGraph to eagerly fetch related entities needed for the user's order detail view.
     * @param orderId ID of the order.
     * @param userId ID of the user.
     * @return An Optional containing the Order if found and belongs to the user.
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "user", "shippingMethod", "promotion"})
    Optional<Order> findByIdAndUserId(Integer orderId, Integer userId);

    // =========================================
    // === METHODS FOR ADMIN / DETAIL VIEWS ===
    // =========================================

    /**
     * Overrides the default findById method to always use an EntityGraph.
     * This ensures that when fetching an order by ID (e.g., for admin detail view or
     * potentially user detail view if not using findByIdAndUserId), the necessary
     * related entities are fetched eagerly to prevent N+1 problems.
     * @param orderId ID of the order.
     * @return An Optional containing the Order with eagerly fetched associations.
     */
    @Override
    @EntityGraph(attributePaths = {
            "orderItems", "orderItems.product", // Items and their products
            "user",                             // User who placed the order (if any)
            "shippingMethod",                   // Shipping method used
            "promotion",                        // Promotion applied (if any)
            "shippingAddress"                   // Saved shipping address (if used)
            // Add "billingAddress" if you have and need it
    })
    Optional<Order> findById(Integer orderId);

    // ==============================================================
    // === NO NEED FOR CUSTOM ADMIN LIST QUERY WITH SPECIFICATION ===
    // ==============================================================
    // The findAll(Specification<Order> spec, Pageable pageable) method
    // is automatically provided by extending JpaSpecificationExecutor.
    // The OrderServiceImpl will use this method along with OrderSpecifications.
    // ==============================================================

}