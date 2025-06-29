// src/main/java/com/example/demo/controller/AdminPromotionController.java
package com.example.demo.controller.admin;

import com.example.demo.dto.promotion.CreateUpdatePromotionDTO;
import com.example.demo.dto.promotion.PromotionDTO;
import com.example.demo.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // Import Map để nhận payload đơn giản cho PATCH

@RestController
@RequestMapping("/api/admin/promotions") // Endpoint cơ sở cho quản lý khuyến mãi bởi admin
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Chỉ Admin mới được truy cập controller này
public class AdminPromotionController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPromotionController.class); // Khởi tạo Logger
    private final PromotionService promotionService;

    /**
     * Lấy danh sách tất cả khuyến mãi với bộ lọc và phân trang.
     * Endpoint: GET /api/admin/promotions
     *
     * @param statusFilter Lọc theo trạng thái ('ACTIVE', 'UPCOMING', 'EXPIRED', 'INACTIVE') - tùy chọn.
     * @param targetTier Lọc theo bậc khách hàng được áp dụng (e.g., 'SILVER') - tùy chọn.
     * @param pageable Thông tin phân trang và sắp xếp (mặc định 10/trang, sắp xếp theo ngày tạo giảm dần).
     * @return ResponseEntity chứa Page<PromotionDTO> và status 200 OK.
     */
    @GetMapping
    public ResponseEntity<Page<PromotionDTO>> getAdminPromotions(
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) String targetTier,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        logger.info("GET /api/admin/promotions - Fetching promotions with filters: status={}, tier={}, pageable={}",
                statusFilter, targetTier, pageable);
        Page<PromotionDTO> promotionPage = promotionService.getAdminPromotions(statusFilter, targetTier, pageable);
        return ResponseEntity.ok(promotionPage);
    }

    /**
     * Lấy chi tiết một khuyến mãi theo ID.
     * Endpoint: GET /api/admin/promotions/{id}
     *
     * @param id ID của khuyến mãi cần lấy.
     * @return ResponseEntity chứa PromotionDTO chi tiết và status 200 OK.
     *         Trả về 404 Not Found nếu không tìm thấy.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable Integer id) {
        logger.info("GET /api/admin/promotions/{} - Fetching promotion details", id);
        PromotionDTO promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(promotion);
    }

    /**
     * Tạo một khuyến mãi mới.
     * Endpoint: POST /api/admin/promotions
     *
     * @param dto Dữ liệu khuyến mãi từ request body (đã được validate).
     * @return ResponseEntity chứa PromotionDTO của khuyến mãi vừa tạo và status 201 Created.
     *         Trả về 400 Bad Request nếu dữ liệu không hợp lệ.
     *         Trả về 409 Conflict nếu mã code bị trùng.
     */
    @PostMapping
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody CreateUpdatePromotionDTO dto) {
        logger.info("POST /api/admin/promotions - Creating new promotion with code: {}", dto.getCode());
        PromotionDTO createdPromotion = promotionService.createPromotion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromotion);
    }

    /**
     * Cập nhật thông tin một khuyến mãi hiện có (cập nhật toàn bộ).
     * Endpoint: PUT /api/admin/promotions/{id}
     * Dùng khi muốn sửa nhiều thông tin cùng lúc qua form chỉnh sửa.
     *
     * @param id ID của khuyến mãi cần cập nhật.
     * @param dto Dữ liệu cập nhật từ request body (đã được validate bởi @Valid).
     * @return ResponseEntity chứa PromotionDTO đã được cập nhật và status 200 OK.
     *         Trả về 400 Bad Request nếu dữ liệu không hợp lệ.
     *         Trả về 404 Not Found nếu ID không tồn tại.
     *         Trả về 409 Conflict nếu cập nhật gây trùng mã code.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable Integer id, @Valid @RequestBody CreateUpdatePromotionDTO dto) {
        logger.info("PUT /api/admin/promotions/{} - Updating promotion fully", id);
        PromotionDTO updatedPromotion = promotionService.updatePromotion(id, dto);
        return ResponseEntity.ok(updatedPromotion);
    }

    /**
     * Cập nhật trạng thái hoạt động (kích hoạt/ngừng) của một khuyến mãi.
     * Endpoint: PATCH /api/admin/promotions/{id}/status
     * Sử dụng PATCH vì đây là cập nhật một phần dữ liệu (chỉ trường isActive).
     *
     * @param id ID của khuyến mãi cần cập nhật trạng thái.
     * @param payload Request body dạng JSON chỉ chứa trường "isActive", ví dụ: {"isActive": true} hoặc {"isActive": false}
     * @return ResponseEntity chứa PromotionDTO đã cập nhật trạng thái và status 200 OK.
     *         Trả về 400 Bad Request nếu payload không hợp lệ (thiếu 'isActive' hoặc giá trị null).
     *         Trả về 404 Not Found nếu ID không tồn tại.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PromotionDTO> togglePromotionStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> payload) { // Nhận Map<String, Boolean>

        // Lấy giá trị boolean từ key "isActive" trong Map payload
        Boolean newStatus = payload.get("isActive");

        // Kiểm tra xem key "isActive" có tồn tại và giá trị có phải là boolean (không null) không
        if (newStatus == null) {
            logger.warn("PATCH /api/admin/promotions/{}/status - Bad request: missing or null 'isActive' field in payload", id);
            // Có thể trả về thông báo lỗi cụ thể hơn
            return ResponseEntity.badRequest().body(null); // Hoặc trả về ErrorDTO
        }

        logger.info("PATCH /api/admin/promotions/{}/status - Setting isActive to {}", id, newStatus);
        // Gọi hàm service mới được thiết kế riêng cho việc cập nhật status
        // (Giả định bạn đã tạo hàm setPromotionStatus trong PromotionService và PromotionServiceImpl)
        PromotionDTO updatedPromotion = promotionService.setPromotionStatus(id, newStatus);
        return ResponseEntity.ok(updatedPromotion);
    }

    /**
     * Endpoint này không nên được sử dụng để toggle trạng thái nữa.
     * Nó có thể được dùng để xóa cứng khuyến mãi (nếu cần và đã xử lý logic ràng buộc).
     * Hiện tại đang đánh dấu là không được phép sử dụng cho việc toggle status.
     *
     * Endpoint: DELETE /api/admin/promotions/{id}
     *
     * @param id ID của khuyến mãi.
     * @return ResponseEntity với status 405 Method Not Allowed.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> handleDeleteRequest(@PathVariable Integer id) {
        logger.warn("DELETE /api/admin/promotions/{} - This endpoint is not intended for status toggling. Use PATCH /status. If actual deletion is needed, implement specific logic.", id);
        // Trả về lỗi 405 để chỉ ra phương thức này không dùng cho mục đích đó
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        // Nếu muốn implement xóa cứng:
        // logger.info("DELETE /api/admin/promotions/{} - Attempting hard delete", id);
        // promotionService.deletePromotionById(id); // Cần tạo hàm này và xử lý ràng buộc
        // return ResponseEntity.noContent().build();
    }
}