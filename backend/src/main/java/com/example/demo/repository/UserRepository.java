// src/main/java/com/example/demo/repository/UserRepository.java
package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< THÊM IMPORT NÀY
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> { // <<< THÊM JpaSpecificationExecutor
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    // --- PHƯƠNG THỨC MỚI CHO ADMIN ---
    /**
     * Tìm kiếm và phân trang danh sách khách hàng (role='CUSTOMER').
     * Hỗ trợ tìm kiếm theo username hoặc fullName (không phân biệt hoa thường).
     * Hỗ trợ lọc theo tier và isActive.
     * @param keyword Từ khóa tìm kiếm (có thể null hoặc rỗng)
     * @param tier Bậc khách hàng để lọc (có thể null)
     * @param isActive Trạng thái hoạt động để lọc (có thể null)
     * @param pageable Thông tin phân trang và sắp xếp
     * @return Trang danh sách khách hàng
     */
    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' " +
            "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(concat('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(concat('%', :keyword, '%'))) " +
            "AND (:tier IS NULL OR u.tier = :tier) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findCustomers(
            @Param("keyword") String keyword,
            @Param("tier") String tier,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    /**
     * Lấy chi tiết khách hàng bằng ID, fetch kèm địa chỉ và đơn hàng (nếu cần).
     * Sử dụng @EntityGraph để tránh N+1 query.
     * @param userId ID của khách hàng
     * @return Optional<User>
     */
    // @EntityGraph(attributePaths = {"addresses", "orders"}) // Uncomment và điều chỉnh nếu cần fetch eager
    Optional<User> findByIdAndRole(Integer userId, String role); // Thêm role để chắc chắn là CUSTOMER
}