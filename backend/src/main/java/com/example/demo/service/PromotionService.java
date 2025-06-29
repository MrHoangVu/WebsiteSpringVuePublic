// src/main/java/com/example/demo/service/PromotionService.java
package com.example.demo.service;

import com.example.demo.dto.promotion.AppliedPromotionDTO;
import com.example.demo.dto.promotion.CreateUpdatePromotionDTO;
import com.example.demo.dto.promotion.PromotionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromotionService {

    // --- Admin Operations ---

    /**
     * Lấy danh sách khuyến mãi cho admin với bộ lọc và phân trang.
     *
     * @param statusFilter Lọc theo trạng thái ('ACTIVE', 'UPCOMING', 'EXPIRED', 'INACTIVE')
     * @param targetTier   Lọc theo bậc khách hàng được áp dụng (e.g., 'SILVER')
     * @param pageable     Thông tin phân trang và sắp xếp
     * @return Trang PromotionDTO
     */
    Page<PromotionDTO> getAdminPromotions(String statusFilter, String targetTier, Pageable pageable); // <<< ĐẢM BẢO CÓ HÀM NÀY

    PromotionDTO getPromotionById(Integer id); // Giữ nguyên tên này cũng được nếu nó dùng cho cả admin

    PromotionDTO createPromotion(CreateUpdatePromotionDTO dto);

    PromotionDTO updatePromotion(Integer id, CreateUpdatePromotionDTO dto);
    // void deletePromotion(Integer id); // Có thể bỏ hàm này nếu dùng update để deactivate

    // --- Customer/Order Operations ---
    AppliedPromotionDTO validateAndCalculateDiscount(String code, String username, String cartSessionId); // <<< SỬA CHỮ KÝ

    void recordPromotionUsage(Integer promotionId);

    /**
     * Cập nhật trạng thái active/inactive cho khuyến mãi.
     *
     * @param id       ID của khuyến mãi.
     * @param isActive Trạng thái mới (true: active, false: inactive).
     * @return PromotionDTO đã cập nhật.
     */
    PromotionDTO setPromotionStatus(Integer id, boolean isActive); // <<< THÊM HÀM NÀY


}