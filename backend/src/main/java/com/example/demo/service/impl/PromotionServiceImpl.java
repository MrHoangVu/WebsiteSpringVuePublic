// src/main/java/com/example/demo/service/impl/PromotionServiceImpl.java
package com.example.demo.service.impl; // Đảm bảo package name đúng với cấu trúc của bạn

import com.example.demo.dto.promotion.AppliedPromotionDTO;
import com.example.demo.dto.promotion.CreateUpdatePromotionDTO;
import com.example.demo.dto.promotion.PromotionDTO;
import com.example.demo.entity.Promotion;
// import com.example.demo.entity.User; // Bỏ import này nếu không check tier ở đây
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PromotionRepository;
// import com.example.demo.repository.UserRepository; // Bỏ import này nếu không check tier ở đây
import com.example.demo.repository.specification.PromotionSpecifications; // Import Specifications
import com.example.demo.service.CartService; // Import CartService
import com.example.demo.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Import Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Đánh dấu là một Service Bean
@RequiredArgsConstructor // Tự động inject các final fields qua constructor
@Transactional // Mặc định các phương thức public sẽ có transaction
public class PromotionServiceImpl implements PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionServiceImpl.class);
    private final PromotionRepository promotionRepository;
    private final CartService cartService; // Inject CartService để lấy subtotal

    // Danh sách các tier hợp lệ (nên đồng bộ với Enum hoặc constant ở nơi khác nếu có)
    private static final List<String> VALID_TIERS = List.of("BRONZE", "SILVER", "GOLD", "DIAMOND");

    // --- Admin Operations ---

    /**
     * Lấy danh sách khuyến mãi cho admin với bộ lọc và phân trang.
     * @param statusFilter Lọc theo trạng thái ('ACTIVE', 'UPCOMING', 'EXPIRED', 'INACTIVE')
     * @param targetTier   Lọc theo bậc khách hàng được áp dụng (e.g., 'SILVER')
     * @param pageable     Thông tin phân trang và sắp xếp
     * @return Trang PromotionDTO
     */
    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public Page<PromotionDTO> getAdminPromotions(String statusFilter, String targetTier, Pageable pageable) {
        logger.info("ADMIN: Fetching promotions with filters - Status: {}, Tier: {}, Pageable: {}", statusFilter, targetTier, pageable);
        Specification<Promotion> spec = PromotionSpecifications.filterByAdminCriteria(statusFilter, targetTier);
        Page<Promotion> promotionPage = promotionRepository.findAll(spec, pageable);
        return promotionPage.map(this::mapToPromotionDTO);
    }

    /**
     * Lấy chi tiết một khuyến mãi theo ID.
     * @param id ID của khuyến mãi.
     * @return PromotionDTO chi tiết.
     * @throws ResourceNotFoundException nếu không tìm thấy.
     */
    @Override
    @Transactional(readOnly = true)
    public PromotionDTO getPromotionById(Integer id) {
        logger.info("Fetching promotion by ID: {}", id);
        Promotion promotion = findPromotionByIdOrThrow(id);
        return mapToPromotionDTO(promotion);
    }

    /**
     * Tạo một khuyến mãi mới.
     * @param dto Dữ liệu khuyến mãi từ request.
     * @return PromotionDTO của khuyến mãi vừa tạo.
     * @throws BadRequestException nếu mã code đã tồn tại.
     */
    @Override
    @Transactional
    public PromotionDTO createPromotion(CreateUpdatePromotionDTO dto) {
        logger.info("Creating new promotion with code: {}", dto.getCode());
        promotionRepository.findByCodeIgnoreCase(dto.getCode()).ifPresent(existing -> {
            throw new BadRequestException("Mã khuyến mãi '" + dto.getCode() + "' đã tồn tại.");
        });

        Promotion promotion = new Promotion();
        BeanUtils.copyProperties(dto, promotion, "targetTiers");
        promotion.setCurrentUsage(0);
        promotion.setTargetTiers(normalizeAndValidateTiers(dto.getTargetTiers()));

        Promotion savedPromotion = promotionRepository.save(promotion);
        logger.info("Promotion created successfully with ID: {}", savedPromotion.getId());
        return mapToPromotionDTO(savedPromotion);
    }

    /**
     * Cập nhật thông tin một khuyến mãi.
     * @param id  ID của khuyến mãi cần cập nhật.
     * @param dto Dữ liệu cập nhật từ request.
     * @return PromotionDTO của khuyến mãi đã cập nhật.
     * @throws ResourceNotFoundException nếu không tìm thấy khuyến mãi.
     * @throws BadRequestException       nếu mã code mới bị trùng với mã khác.
     */
    @Override
    @Transactional
    public PromotionDTO updatePromotion(Integer id, CreateUpdatePromotionDTO dto) {
        logger.info("Updating promotion with ID: {}", id);
        Promotion promotion = findPromotionByIdOrThrow(id);

        if (!promotion.getCode().equalsIgnoreCase(dto.getCode())) {
            promotionRepository.findByCodeIgnoreCase(dto.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("Mã khuyến mãi '" + dto.getCode() + "' đã tồn tại.");
                }
            });
        }

        BeanUtils.copyProperties(dto, promotion, "id", "createdAt", "currentUsage", "targetTiers");
        promotion.setTargetTiers(normalizeAndValidateTiers(dto.getTargetTiers()));

        Promotion updatedPromotion = promotionRepository.save(promotion);
        logger.info("Promotion updated successfully with ID: {}", updatedPromotion.getId());
        return mapToPromotionDTO(updatedPromotion);
    }

    /**
     * Cập nhật trạng thái active/inactive cho khuyến mãi.
     * @param id ID của khuyến mãi.
     * @param isActive Trạng thái mới (true: active, false: inactive).
     * @return PromotionDTO đã cập nhật.
     */
    @Override
    @Transactional
    public PromotionDTO setPromotionStatus(Integer id, boolean isActive) {
        logger.info("ADMIN: Setting status for promotion ID: {} to {}", id, isActive);
        Promotion promotion = findPromotionByIdOrThrow(id);

        if (promotion.getIsActive() != isActive) {
            promotion.setIsActive(isActive);
            Promotion updatedPromotion = promotionRepository.save(promotion);
            logger.info("Promotion ID {} status updated successfully to {}", id, isActive);
            return mapToPromotionDTO(updatedPromotion);
        } else {
            logger.info("Promotion ID {} already has status isActive={}. No change needed.", id, isActive);
            return mapToPromotionDTO(promotion);
        }
    }

    // --- Customer/Order Operations ---

    /**
     * Xác thực mã khuyến mãi và tính toán giảm giá. Lấy tổng tiền từ CartService.
     * @param code Mã khuyến mãi.
     * @param username Username (null nếu là guest).
     * @param cartSessionId Session ID (null nếu là user).
     * @return AppliedPromotionDTO chứa kết quả.
     */
    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO validateAndCalculateDiscount(String code, String username, String cartSessionId) {
        logger.info("Validating promotion code (public API): '{}' for user: '{}', session: '{}'", code, username, cartSessionId);

        // === LẤY TỔNG TIỀN TỪ BACKEND ===
        BigDecimal currentOrderTotal = cartService.calculateSubtotalForCart(username, cartSessionId);
        logger.debug("Calculated currentOrderTotal from backend: {}", currentOrderTotal);
        // ================================

        if (currentOrderTotal == null || currentOrderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Cart is empty or subtotal is zero/negative. Cannot apply promotion.");
            return new AppliedPromotionDTO(false, "Giỏ hàng trống hoặc không có sản phẩm hợp lệ để áp dụng mã.", null, BigDecimal.ZERO, currentOrderTotal != null ? currentOrderTotal : BigDecimal.ZERO, null);
        }

        Optional<Promotion> promoOpt = promotionRepository.findByCodeIgnoreCase(code);

        // 1. Check Exists
        if (promoOpt.isEmpty()) {
            logger.warn("Validation failed: Code '{}' not found.", code);
            return new AppliedPromotionDTO(false, "Mã khuyến mãi không hợp lệ.", null, BigDecimal.ZERO, currentOrderTotal, null);
        }
        Promotion promotion = promoOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // 2. Check Active
        if (promotion.getIsActive() == null || !promotion.getIsActive()) {
            logger.warn("Validation failed: Code '{}' (ID: {}) is not active.", code, promotion.getId());
            return new AppliedPromotionDTO(false, "Mã khuyến mãi đã hết hạn hoặc không hoạt động.", null, BigDecimal.ZERO, currentOrderTotal, null);
        }
        // 3. Check Date Range
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            logger.warn("Validation failed: Code '{}' (ID: {}) is outside the valid date range ({} - {}). Current time: {}", code, promotion.getId(), promotion.getStartDate(), promotion.getEndDate(), now);
            return new AppliedPromotionDTO(false, "Mã khuyến mãi đã hết hạn hoặc chưa đến ngày áp dụng.", null, BigDecimal.ZERO, currentOrderTotal, null);
        }
        // 4. Check Usage Limit
        if (promotion.getMaxUsage() != null && promotion.getCurrentUsage() >= promotion.getMaxUsage()) {
            logger.warn("Validation failed: Code '{}' (ID: {}) has reached maximum usage limit ({}).", code, promotion.getId(), promotion.getMaxUsage());
            return new AppliedPromotionDTO(false, "Mã khuyến mãi đã hết lượt sử dụng.", null, BigDecimal.ZERO, currentOrderTotal, null);
        }
        // 5. Check Minimum Order Value
        if (promotion.getMinOrderValue() != null) {
            logger.debug("Comparing backend orderTotal [{}] with minOrderValue [{}] for promotion ID {}",
                    currentOrderTotal, promotion.getMinOrderValue(), promotion.getId());
            if (currentOrderTotal.compareTo(promotion.getMinOrderValue()) < 0) {
                logger.warn("Validation failed: Order total is less than minimum required.");
                return new AppliedPromotionDTO(false, "Đơn hàng chưa đạt giá trị tối thiểu ("+ formatCurrencyInternal(promotion.getMinOrderValue()) +") để áp dụng mã này.", null, BigDecimal.ZERO, currentOrderTotal, null);
            }
        }

        // Tier check is done in CheckoutService

        // 6. Calculate Discount
        BigDecimal discountAmount = calculateDiscount(promotion, currentOrderTotal);
        BigDecimal finalAmount = currentOrderTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) { finalAmount = BigDecimal.ZERO; discountAmount = currentOrderTotal; }

        logger.info("Validation successful for code '{}' (ID: {}). Discount: {}, Final amount: {}", code, promotion.getId(), discountAmount, finalAmount);
        return new AppliedPromotionDTO(true, "Áp dụng mã khuyến mãi thành công!", promotion.getCode(), discountAmount, finalAmount, promotion.getId());
    }

    /**
     * Ghi nhận một lượt sử dụng cho mã khuyến mãi.
     * @param promotionId ID của khuyến mãi đã được sử dụng.
     */
    @Override
    @Transactional
    public void recordPromotionUsage(Integer promotionId) {
        if (promotionId == null) return;
        logger.info("Recording usage for promotion ID: {}", promotionId);
        Promotion promotion = findPromotionByIdOrThrow(promotionId);

        if (promotion.getMaxUsage() != null && promotion.getCurrentUsage() >= promotion.getMaxUsage()) {
            logger.error("Concurrency Issue? Attempted to record usage for fully used promotion ID: {}", promotionId);
            return;
        }
        promotion.setCurrentUsage(promotion.getCurrentUsage() + 1);
        promotionRepository.save(promotion);
        logger.info("Successfully recorded usage for promotion ID: {}. New usage count: {}", promotionId, promotion.getCurrentUsage());
    }

    // --- Helper Methods ---

    private Promotion findPromotionByIdOrThrow(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));
    }

    private PromotionDTO mapToPromotionDTO(Promotion promotion) {
        if (promotion == null) return null;
        PromotionDTO dto = new PromotionDTO();
        BeanUtils.copyProperties(promotion, dto);
        return dto;
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderTotal) {
        if (promotion == null || promotion.getDiscountValue() == null || promotion.getDiscountType() == null || orderTotal == null) {
            return BigDecimal.ZERO;
        }
        if ("PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())) {
            BigDecimal discountPercent = promotion.getDiscountValue().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            return orderTotal.multiply(discountPercent).setScale(0, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
            return promotion.getDiscountValue().min(orderTotal);
        }
        return BigDecimal.ZERO;
    }

    private String normalizeAndValidateTiers(String inputTiers) {
        if (inputTiers == null || inputTiers.isBlank()) {
            return null;
        }
        String normalizedTiers = Arrays.stream(inputTiers.split(","))
                .map(String::trim)
                .filter(tier -> !tier.isEmpty())
                .map(String::toUpperCase)
                .filter(VALID_TIERS::contains)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
        return normalizedTiers.isEmpty() ? null : normalizedTiers;
    }

    private String formatCurrencyInternal(BigDecimal value) {
        if (value == null) return "0 đ";
        return new java.text.DecimalFormat("#,##0 đ").format(value);
    }
}