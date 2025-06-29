// src/main/java/com/example/demo/service/impl/CheckoutServiceImpl.java
package com.example.demo.service.impl; // **Lưu ý: Thường đặt impl vào package con "impl"**

import com.example.demo.dto.checkout.CheckoutInfoDTO;
import com.example.demo.dto.checkout.PlaceOrderRequestDTO;
import com.example.demo.dto.order.OrderSummaryDTO;
import com.example.demo.dto.shipping.ShippingMethodDTO;
import com.example.demo.dto.user.AddressDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.CartService;       // Import Interface
import com.example.demo.service.CheckoutService;   // Import Interface
import com.example.demo.service.PromotionService; // Import Interface (nếu đã tách)
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service // Annotation @Service chuyển sang đây
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService { // Implement interface

    private static final Logger logger = LoggerFactory.getLogger(CheckoutServiceImpl.class); // Logger dùng tên class Impl
    private static final String CART_SESSION_HEADER = "X-Cart-Session-Id";

    // --- Dependencies --- (Giữ nguyên)
    private final CartService cartService;
    private final PromotionService promotionService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Lớp nội bộ để giữ thông tin khuyến mãi (Giữ trong Impl)
     */
    @RequiredArgsConstructor
    @Getter
    private static class AppliedPromotionInfo {
        private final BigDecimal discountAmount;
        private final Promotion promotionEntity;
    }

    // --- Public Methods from Interface ---

    @Override // Thêm @Override
    @Transactional(readOnly = true)
    public CheckoutInfoDTO getCheckoutInfo(String username) {
        logger.info("Fetching checkout info for user: {}", username);
        User user = findUserByUsernameOrThrow(username);

        CheckoutInfoDTO info = new CheckoutInfoDTO();

        // Lấy địa chỉ đã lưu
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        info.setSavedAddresses(addresses.stream().map(this::mapToAddressDTO).collect(Collectors.toList()));

        // Tìm địa chỉ mặc định
        info.setDefaultShippingAddress(addresses.stream()
                .filter(a -> a.getIsDefaultShipping() != null && a.getIsDefaultShipping())
                .findFirst()
                .map(this::mapToAddressDTO)
                .orElse(null));

        // Lấy PTVC khả dụng
        List<ShippingMethod> shippingMethods = shippingMethodRepository.findByIsActiveTrue();
        info.setAvailableShippingMethods(shippingMethods.stream().map(this::mapToShippingMethodDTO).collect(Collectors.toList()));

        return info;
    }

    @Override // Thêm @Override
    @Transactional // Giữ nguyên @Transactional ở đây
    public OrderSummaryDTO placeOrder(PlaceOrderRequestDTO request, String username) {
        String cartSessionId = getCartSessionIdFromRequest();
        logger.info("Placing order. User: '{}', Session: '{}', Request: {}", username, cartSessionId, request);

        // 1. Lấy User hoặc kiểm tra thông tin Guest
        User user = (username != null) ? findUserByUsernameOrThrow(username) : null;
        validateGuestInfo(request, user);

        // 2. Lấy giỏ hàng THỰC TẾ từ CartService
        Cart cart = cartService.getOrCreateCart(username, cartSessionId);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống.");
        }

        // 3. Fetch và kiểm tra Products trong giỏ hàng
        Map<Integer, Product> productMap = validateCartItemsAndGetProductMap(cart);

        // 4. Lấy và kiểm tra Shipping Method
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .filter(sm -> sm.getIsActive() != null && sm.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Active Shipping Method", "id", request.getShippingMethodId()));
        BigDecimal shippingCost = shippingMethod.getBaseCost();

        // 5. Tính Subtotal từ CartService
        BigDecimal subtotal = cartService.calculateSubtotalForCart(username, cartSessionId);
        logger.debug("Calculated subtotal: {}", subtotal);

        // 6. Xác thực và áp dụng Khuyến mãi
        AppliedPromotionInfo promotionInfo = applyPromotionDuringCheckout(
                request.getAppliedPromotionCode(),
                subtotal,
                user
        );
        logger.debug("Checkout - Promotion info: Discount={}, EntityId={}",
                promotionInfo.getDiscountAmount(),
                promotionInfo.getPromotionEntity() != null ? promotionInfo.getPromotionEntity().getId() : "null"
        );

        // 7. Tính Tổng tiền cuối cùng
        BigDecimal finalAmount = calculateFinalAmount(subtotal, shippingCost, promotionInfo.getDiscountAmount());
        logger.debug("Calculated final amount: {}", finalAmount);

        // 8. Tạo Order Entity
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(finalAmount);
        order.setShippingMethod(shippingMethod);
        order.setShippingCost(shippingCost);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("PENDING");
        order.setOrderNote(request.getOrderNote());
        order.setDiscountAmount(promotionInfo.getDiscountAmount());
        order.setPromotion(promotionInfo.getPromotionEntity());

        // 9. Xử lý và Sao chép Địa chỉ vào Order
        populateOrderAddress(order, request, user);
        if (user == null && request.getShippingAddressInput() != null) {
            order.setGuestEmail(request.getShippingAddressInput().getRecipientEmail());
        }

        // 10. Tạo OrderItems và Cập nhật Stock
        Set<OrderItem> orderItemsSet = createOrderItemsAndUpdateStock(order, cart, productMap);
        order.setOrderItems(orderItemsSet);

        // 11. Lưu Order vào Database
        Order savedOrder = orderRepository.save(order);
        logger.info("Order placed successfully with ID: {}", savedOrder.getId());

        // 12. Ghi nhận sử dụng Khuyến mãi
        if (promotionInfo.getPromotionEntity() != null) {
            promotionService.recordPromotionUsage(promotionInfo.getPromotionEntity().getId());
        }

        // 13. Xóa Giỏ hàng
        cartService.clearCart(username, cartSessionId);
        logger.info("Cart cleared for user: '{}', session: '{}'", username, cartSessionId);

        // 14. (Tùy chọn) Tạo URL thanh toán online
        String paymentUrl = null; // TODO

        // 15. Trả về Order Summary
        return OrderSummaryDTO.builder()
                .orderId(savedOrder.getId())
                .orderStatus(savedOrder.getStatus())
                .orderDate(savedOrder.getOrderDate())
                .finalAmount(savedOrder.getTotalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .successMessage("Đặt hàng thành công! Mã đơn hàng của bạn là #" + savedOrder.getId())
                .paymentUrl(paymentUrl)
                .build();
    }

    // --- Helper Methods --- (Giữ nguyên tất cả các phương thức private)

    private User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private void validateGuestInfo(PlaceOrderRequestDTO request, User user) {
        if (user == null) { // Chỉ kiểm tra cho guest
            if (request.getShippingAddressInput() == null) {
                throw new BadRequestException("Vui lòng nhập thông tin địa chỉ giao hàng.");
            }
            if (request.getShippingAddressInput().getRecipientEmail() == null || request.getShippingAddressInput().getRecipientEmail().isBlank()) {
                throw new BadRequestException("Vui lòng nhập email người nhận khi đặt hàng với tư cách khách.");
            }
            // Thêm kiểm tra các trường bắt buộc khác của địa chỉ guest
            if (request.getShippingAddressInput().getRecipientName() == null || request.getShippingAddressInput().getRecipientName().isBlank()) throw new BadRequestException("Vui lòng nhập tên người nhận.");
            if (request.getShippingAddressInput().getRecipientPhone() == null || request.getShippingAddressInput().getRecipientPhone().isBlank()) throw new BadRequestException("Vui lòng nhập số điện thoại người nhận.");
            if (request.getShippingAddressInput().getStreetAddress() == null || request.getShippingAddressInput().getStreetAddress().isBlank()) throw new BadRequestException("Vui lòng nhập địa chỉ cụ thể.");
            if (request.getShippingAddressInput().getDistrict() == null || request.getShippingAddressInput().getDistrict().isBlank()) throw new BadRequestException("Vui lòng nhập Quận/Huyện.");
            if (request.getShippingAddressInput().getCity() == null || request.getShippingAddressInput().getCity().isBlank()) throw new BadRequestException("Vui lòng nhập Tỉnh/Thành phố.");
        }
    }

    private Map<Integer, Product> validateCartItemsAndGetProductMap(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống.");
        }
        Set<Integer> productIds = cart.getCartItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            throw new BadRequestException("Không thể xác định sản phẩm trong giỏ hàng.");
        }

        List<Product> productsInCart = productRepository.findAllById(productIds);
        Map<Integer, Product> productMap = productsInCart.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (CartItem item : cart.getCartItems()) {
            Product product = productMap.get(item.getProduct().getId());
            if (product == null) {
                throw new BadRequestException("Một sản phẩm trong giỏ hàng của bạn không còn tồn tại.");
            }
            if (product.getIsActive() == null || !product.getIsActive()) {
                throw new BadRequestException("Sản phẩm '" + product.getName() + "' hiện không có sẵn để bán.");
            }
            if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Không đủ số lượng tồn kho cho sản phẩm '" + product.getName() + "'. Hiện có: " + (product.getStock() != null ? product.getStock() : 0) + ", yêu cầu: " + item.getQuantity());
            }
        }
        return productMap;
    }

    private AppliedPromotionInfo applyPromotionDuringCheckout(String promotionCode, BigDecimal subtotal, User user) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        Promotion promotionEntity = null;

        if (promotionCode != null && !promotionCode.isBlank()) {
            logger.debug("Checkout - Applying promotion code: {}", promotionCode);
            Optional<Promotion> promoOpt = promotionRepository.findByCodeIgnoreCase(promotionCode);

            if (promoOpt.isEmpty()) {
                throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' không hợp lệ.");
            }
            promotionEntity = promoOpt.get();
            LocalDateTime now = LocalDateTime.now();

            if (promotionEntity.getIsActive() == null || !promotionEntity.getIsActive()) {
                throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' không còn hoạt động.");
            }
            if (now.isBefore(promotionEntity.getStartDate()) || now.isAfter(promotionEntity.getEndDate())) {
                throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' đã hết hạn hoặc chưa đến ngày áp dụng.");
            }
            if (promotionEntity.getMaxUsage() != null && promotionEntity.getCurrentUsage() >= promotionEntity.getMaxUsage()) {
                throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' đã hết lượt sử dụng.");
            }
            if (promotionEntity.getMinOrderValue() != null && subtotal.compareTo(promotionEntity.getMinOrderValue()) < 0) {
                throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu (" + promotionEntity.getMinOrderValue() + ") để áp dụng mã '" + promotionCode + "'.");
            }
            if (promotionEntity.getTargetTiers() != null && !promotionEntity.getTargetTiers().isEmpty()) {
                if (user == null || user.getTier() == null) {
                    logger.warn("Checkout - Promotion ID {} requires specific tiers, but user is guest or has no tier.", promotionEntity.getId());
                    throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' này chỉ dành cho thành viên hạng cụ thể.");
                }
                List<String> allowedTiers = Arrays.asList(promotionEntity.getTargetTiers().toUpperCase().split(","));
                if (!allowedTiers.contains(user.getTier().toUpperCase())) {
                    logger.warn("Checkout - Promotion ID {} requires tiers '{}', but user ID {} has tier '{}'.", promotionEntity.getId(), promotionEntity.getTargetTiers(), user.getId(), user.getTier());
                    throw new BadRequestException("Mã khuyến mãi '" + promotionCode + "' không áp dụng cho hạng thành viên của bạn (" + user.getTier() + ").");
                }
                logger.debug("Checkout - User tier '{}' is eligible for promotion tiers '{}'.", user.getTier(), promotionEntity.getTargetTiers());
            }

            discountAmount = calculateDiscountInternal(promotionEntity, subtotal);
        }
        return new AppliedPromotionInfo(discountAmount, promotionEntity);
    }


    private BigDecimal calculateFinalAmount(BigDecimal subtotal, BigDecimal shippingCost, BigDecimal discountAmount) {
        BigDecimal finalAmount = subtotal.add(shippingCost).subtract(discountAmount);
        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }

    private void populateOrderAddress(Order order, PlaceOrderRequestDTO request, User user) {
        Address shippingAddressEntity = null;

        if (request.getSelectedShippingAddressId() != null) {
            if (user == null) {
                throw new BadRequestException("Không thể chọn địa chỉ đã lưu khi đặt hàng với tư cách khách.");
            }
            shippingAddressEntity = addressRepository.findById(request.getSelectedShippingAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipping Address", "id", request.getSelectedShippingAddressId()));
            if (!shippingAddressEntity.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Địa chỉ giao hàng đã chọn không thuộc về tài khoản này.");
            }
            logger.debug("Using saved address ID: {}", shippingAddressEntity.getId());
            order.setShippingRecipientName(shippingAddressEntity.getRecipientName());
            order.setShippingRecipientPhone(shippingAddressEntity.getRecipientPhone());
            order.setShippingStreetAddress(shippingAddressEntity.getStreetAddress());
            order.setShippingWard(shippingAddressEntity.getWard());
            order.setShippingDistrict(shippingAddressEntity.getDistrict());
            order.setShippingCity(shippingAddressEntity.getCity());
            order.setShippingAddress(shippingAddressEntity);

        } else if (request.getShippingAddressInput() != null) {
            PlaceOrderRequestDTO.ShippingAddressInput input = request.getShippingAddressInput();
            logger.debug("Using new shipping address input: {}", input);
            order.setShippingRecipientName(input.getRecipientName());
            order.setShippingRecipientPhone(input.getRecipientPhone());
            order.setShippingStreetAddress(input.getStreetAddress());
            order.setShippingWard(input.getWard());
            order.setShippingDistrict(input.getDistrict());
            order.setShippingCity(input.getCity());
            // TODO (Optional): Save new address logic

        } else {
            throw new BadRequestException("Thiếu thông tin địa chỉ giao hàng.");
        }

        order.setCustomerName(order.getShippingRecipientName());
        order.setCustomerPhone(order.getShippingRecipientPhone());
        order.setCustomerAddress(String.format("%s, %s, %s, %s",
                order.getShippingStreetAddress(),
                order.getShippingWard() != null ? order.getShippingWard() : "",
                order.getShippingDistrict(),
                order.getShippingCity()
        ).replaceAll(", ,", ",").replaceAll("^, ", "").replaceAll(", $", "").trim());
    }


    private Set<OrderItem> createOrderItemsAndUpdateStock(Order order, Cart cart, Map<Integer, Product> productMap) {
        Set<OrderItem> orderItemsSet = new HashSet<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productMap.get(cartItem.getProduct().getId());
            if (product == null || product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                logger.error("Stock inconsistency detected for product ID {} during OrderItem creation! Required: {}, Available: {}",
                        cartItem.getProduct().getId(), cartItem.getQuantity(), (product != null ? product.getStock() : "N/A"));
                throw new BadRequestException("Số lượng tồn kho đã thay đổi cho sản phẩm: " + (product != null ? product.getName() : "ID " + cartItem.getProduct().getId()));
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            orderItemsSet.add(orderItem);

            int newStock = product.getStock() - cartItem.getQuantity();
            product.setStock(newStock);
            logger.debug("Decreasing stock for product ID {}: {} -> {}", product.getId(), product.getStock() + cartItem.getQuantity(), newStock);
        }
        return orderItemsSet;
    }


    private AddressDTO mapToAddressDTO(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    private ShippingMethodDTO mapToShippingMethodDTO(ShippingMethod method) {
        if (method == null) return null;
        ShippingMethodDTO dto = new ShippingMethodDTO();
        BeanUtils.copyProperties(method, dto);
        String estimated = "Không xác định";
        if (method.getEstimatedDaysMin() != null && method.getEstimatedDaysMax() != null) {
            if (method.getEstimatedDaysMin().equals(method.getEstimatedDaysMax())) {
                estimated = method.getEstimatedDaysMin() + " ngày";
            } else {
                estimated = method.getEstimatedDaysMin() + "-" + method.getEstimatedDaysMax() + " ngày";
            }
        } else if (method.getEstimatedDaysMin() != null) {
            estimated = "Từ " + method.getEstimatedDaysMin() + " ngày";
        } else if (method.getEstimatedDaysMax() != null) {
            estimated = "Tối đa " + method.getEstimatedDaysMax() + " ngày";
        }
        dto.setEstimatedDelivery(estimated);
        return dto;
    }

    private String getCartSessionIdFromRequest() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            return request.getHeader(CART_SESSION_HEADER);
        }
        logger.warn("Could not get HttpServletRequest from RequestContextHolder. Cart session ID might be missing.");
        return null;
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                return attributes.getRequest();
            }
        } catch (IllegalStateException e) {
            logger.debug("Not running in a request context.", e);
        }
        return null;
    }

    private BigDecimal calculateDiscountInternal(Promotion promotion, BigDecimal orderTotal) {
        if (promotion == null || promotion.getDiscountValue() == null || promotion.getDiscountType() == null) {
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
}