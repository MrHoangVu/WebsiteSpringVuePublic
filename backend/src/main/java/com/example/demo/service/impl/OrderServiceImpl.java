// src/main/java/com/example/demo/service/impl/OrderServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.order.*;
import com.example.demo.entity.*; // Import các Entity
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedAccessException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.specification.OrderSpecifications; // <<< Import Specifications
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService; // <<< Import UserService để cập nhật chi tiêu
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // <<< Import Specification
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.math.BigDecimal;
import java.time.LocalDate; // Import LocalDate
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service // Đánh dấu là Service Bean
@RequiredArgsConstructor // Tự động inject các final fields qua constructor
@Transactional // Mặc định các phương thức public sẽ có transaction
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    // --- Dependencies ---
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserService userService; // Inject UserService để cập nhật chi tiêu và bậc KH

    // --- User Methods ---

    /**
     * Lấy tóm tắt đơn hàng cho người dùng đang đăng nhập.
     *
     * @param orderId  ID đơn hàng.
     * @param username Username của người dùng.
     * @return OrderSummaryDTO.
     * @throws ResourceNotFoundException   nếu không tìm thấy đơn hàng.
     * @throws UnauthorizedAccessException nếu user không có quyền xem đơn hàng này.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrderSummaryForUser(Integer orderId, String username) {
        logger.debug("Fetching order summary for ID: {} and user: {}", orderId, username);
        User user = findUserByUsernameOrThrow(username);
        // Sử dụng findByIdAndUserId để đảm bảo đơn hàng thuộc về user
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Kiểm tra lại quyền sở hữu (dù query đã check nhưng để chắc chắn)
        // if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
        //     throw new UnauthorizedAccessException("You do not have permission to view this order summary.");
        // }

        return mapToOrderSummaryDTO(order);
    }

    /**
     * Lấy danh sách đơn hàng của người dùng đang đăng nhập (phân trang).
     *
     * @param username Username của người dùng.
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Trang OrderListDTO.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderListDTO> getOrdersForUser(String username, Pageable pageable) {
        logger.info("Fetching orders for user: {} with pageable: {}", username, pageable);
        User user = findUserByUsernameOrThrow(username);

        // --- SỬA Ở ĐÂY ---
        // Lấy thông tin trang từ pageable gốc, nhưng bỏ thông tin sort
        // vì tên phương thức repository đã bao gồm sort rồi.
        Pageable pageRequestWithoutSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
                // Không truyền Sort vào đây nữa
        );
        // -----------------

        // Gọi phương thức repository với Pageable đã bỏ Sort
        Page<Order> orderPage = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId(), pageRequestWithoutSort); // <<< Truyền pageable mới
        logger.debug("Found {} orders on page {} for user ID {}", orderPage.getNumberOfElements(), pageable.getPageNumber(), user.getId());

        return orderPage.map(this::mapToOrderListDTO);
    }

    /**
     * Lấy chi tiết đầy đủ đơn hàng cho người dùng đang đăng nhập.
     *
     * @param orderId  ID đơn hàng.
     * @param username Username của người dùng.
     * @return UserOrderDetailDTO.
     * @throws ResourceNotFoundException   nếu không tìm thấy đơn hàng.
     * @throws UnauthorizedAccessException nếu user không có quyền xem đơn hàng này.
     */
    @Override
    @Transactional(readOnly = true)
    public UserOrderDetailDTO getUserOrderDetail(Integer orderId, String username) {
        logger.debug("USER: Fetching detail for order ID: {} and user: {}", orderId, username);
        User user = findUserByUsernameOrThrow(username);
        // Dùng findByIdAndUserId để đảm bảo đơn hàng thuộc về user và fetch đủ thông tin
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Không cần kiểm tra quyền sở hữu lại ở đây vì query đã làm

        return mapToUserOrderDetailDTO(order);
    }

    // --- Admin Methods ---

    /**
     * Lấy danh sách đơn hàng cho admin với bộ lọc, tìm kiếm và phân trang.
     *
     * @param keyword   Từ khóa tìm kiếm (id, user info, guest info, recipient info).
     * @param status    Lọc theo trạng thái đơn hàng.
     * @param startDate Lọc từ ngày đặt hàng.
     * @param endDate   Lọc đến ngày đặt hàng.
     * @param pageable  Thông tin phân trang và sắp xếp.
     * @return Trang AdminOrderListDTO.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdminOrderListDTO> getAllOrders(String keyword, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        logger.info("ADMIN: Fetching orders with filters - Keyword: '{}', Status: {}, StartDate: {}, EndDate: {}, Pageable: {}",
                keyword, status, startDate, endDate, pageable);

        // Tạo Specification dựa trên các tiêu chí lọc
        Specification<Order> spec = OrderSpecifications.filterByAdminCriteria(keyword, status, startDate, endDate);

        // Gọi repository với Specification và Pageable
        // Lưu ý: Cần đảm bảo fetch đủ thông tin User để map sang DTO list.
        // Nếu gặp N+1 query, cần thêm @EntityGraph vào Specification hoặc phương thức repository.
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        logger.debug("ADMIN: Found {} total orders matching criteria.", orderPage.getTotalElements());
        // Map sang DTO list cho admin
        return orderPage.map(this::mapToAdminOrderListDTO);
    }

    /**
     * Lấy chi tiết đầy đủ đơn hàng cho admin xem.
     *
     * @param orderId ID đơn hàng.
     * @return AdminOrderDetailDTO.
     * @throws ResourceNotFoundException nếu không tìm thấy đơn hàng.
     */
    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailDTO getOrderDetailForAdmin(Integer orderId) {
        logger.info("ADMIN: Fetching detail for order ID: {}", orderId);
        // Dùng findById đã override với @EntityGraph để fetch đủ thông tin
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToAdminOrderDetailDTO(order);
    }

    /**
     * Cập nhật trạng thái của một đơn hàng.
     * Đồng thời cập nhật tổng chi tiêu và bậc của khách hàng nếu đơn hàng chuyển sang COMPLETED.
     *
     * @param orderId   ID đơn hàng.
     * @param newStatus Trạng thái mới.
     * @return Order entity đã được cập nhật.
     * @throws ResourceNotFoundException nếu không tìm thấy đơn hàng.
     * @throws BadRequestException       nếu việc chuyển trạng thái không hợp lệ.
     */
    @Override
    @Transactional
    public Order updateOrderStatus(Integer orderId, String newStatus) {
        logger.info("ADMIN: Updating status for order ID: {} to '{}'", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        String oldStatus = order.getStatus(); // Lưu trạng thái cũ để kiểm tra

        // Kiểm tra tính hợp lệ của việc chuyển trạng thái
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            logger.warn("ADMIN: Invalid status transition from {} to {} for order ID {}", oldStatus, newStatus, orderId);
            throw new BadRequestException("Không thể chuyển trạng thái đơn hàng từ '" + formatStatus(oldStatus) + "' sang '" + formatStatus(newStatus) + "'.");
        }

        // Cập nhật trạng thái
        order.setStatus(newStatus.toUpperCase()); // Chuẩn hóa trạng thái mới
        Order updatedOrder = orderRepository.save(order);
        logger.info("ADMIN: Order ID {} status updated successfully to {}", orderId, newStatus);

        // === CẬP NHẬT TỔNG CHI TIÊU & BẬC KH KHI ĐƠN HÀNG HOÀN THÀNH ===
        // Chỉ cập nhật khi trạng thái MỚI là COMPLETED và trạng thái CŨ KHÔNG PHẢI là COMPLETED
        if ("COMPLETED".equalsIgnoreCase(newStatus) && (oldStatus == null || !"COMPLETED".equalsIgnoreCase(oldStatus))) {
            if (updatedOrder.getUser() != null && updatedOrder.getTotalAmount() != null) {
                logger.info("Order ID {} completed. Updating total spent and tier for user ID: {}", orderId, updatedOrder.getUser().getId());
                // Gọi service user để cập nhật (đã inject UserService)
                try {
                    userService.updateTotalSpentAndTier(updatedOrder.getUser().getId(), updatedOrder.getTotalAmount());
                } catch (Exception e) {
                    // Log lỗi nhưng không nên để transaction rollback chỉ vì lỗi cập nhật tier/spending
                    logger.error("Error updating total spent/tier for user ID {} after order {} completion: {}",
                            updatedOrder.getUser().getId(), orderId, e.getMessage(), e);
                }
            } else {
                logger.warn("Order ID {} completed but user or total amount is null. Skipping spending/tier update.", orderId);
            }
        }
        // =================================================================

        // TODO: Trigger events nếu cần (gửi email, thông báo,...)
        // applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(this, updatedOrder));

        return updatedOrder; // Trả về entity đã cập nhật
    }

    // --- Helper Methods ---

    /**
     * Tìm User theo username hoặc ném ResourceNotFoundException.
     */
    private User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Map Order entity sang OrderListDTO (cho danh sách đơn hàng của user).
     */
    private OrderListDTO mapToOrderListDTO(Order order) {
        if (order == null) return null;
        OrderListDTO dto = new OrderListDTO();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());

        // Map một vài item đầu tiên để preview
        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .limit(3) // Giới hạn số lượng item preview
                    .map(this::mapToOrderItemSummaryDTO)
                    .collect(Collectors.toList()));
        } else {
            logger.warn("Order ID {} has null OrderItems.", order.getId());
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    /**
     * Map OrderItem entity sang OrderItemSummaryDTO.
     */
    private OrderItemSummaryDTO mapToOrderItemSummaryDTO(OrderItem item) {
        if (item == null) return null;
        OrderItemSummaryDTO itemDto = new OrderItemSummaryDTO();
        itemDto.setQuantity(item.getQuantity());
        itemDto.setPriceAtPurchase(item.getPriceAtPurchase());

        Product product = item.getProduct(); // Giả định product đã được fetch (EAGER hoặc EntityGraph)
        if (product != null) {
            itemDto.setProductId(product.getId());
            itemDto.setProductName(product.getName());
            itemDto.setProductImageUrl(product.getImageUrl());
        } else {
            // Xử lý trường hợp sản phẩm bị null (có thể do đã bị xóa cứng)
            logger.warn("OrderItem ID {} references a null Product.", item.getId());
            itemDto.setProductId(null); // Hoặc một giá trị đặc biệt
            itemDto.setProductName("Sản phẩm không tồn tại");
            itemDto.setProductImageUrl(null); // Hoặc ảnh placeholder
        }
        return itemDto;
    }

    /**
     * Map Order entity sang OrderSummaryDTO (cho trang đặt hàng thành công).
     */
    private OrderSummaryDTO mapToOrderSummaryDTO(Order order) {
        if (order == null) return null;
        return OrderSummaryDTO.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .orderDate(order.getOrderDate())
                .finalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                // Không cần successMessage ở đây, nó nên được tạo ở CheckoutService
                // paymentUrl cũng được tạo ở CheckoutService
                .build();
    }

    /**
     * Map Order entity sang AdminOrderListDTO (cho danh sách đơn hàng admin).
     */
    private AdminOrderListDTO mapToAdminOrderListDTO(Order order) {
        if (order == null) return null;
        AdminOrderListDTO dto = new AdminOrderListDTO();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());

        // Xác định thông tin khách hàng
        if (order.getUser() != null) {
            String name = StringUtils.hasText(order.getUser().getFullName()) ? order.getUser().getFullName() : order.getUser().getUsername();
            dto.setCustomerInfo(name + " (User)");
        } else if (StringUtils.hasText(order.getGuestEmail())) {
            String name = StringUtils.hasText(order.getShippingRecipientName()) ? order.getShippingRecipientName() : order.getGuestEmail();
            dto.setCustomerInfo(name + " (Guest)");
        } else {
            dto.setCustomerInfo(order.getShippingRecipientName() != null ? order.getShippingRecipientName() + " (Guest?)" : "N/A");
        }
        return dto;
    }

    /**
     * Map Order entity sang AdminOrderDetailDTO (cho chi tiết đơn hàng admin).
     */
    private AdminOrderDetailDTO mapToAdminOrderDetailDTO(Order order) {
        if (order == null) return null;
        AdminOrderDetailDTO dto = new AdminOrderDetailDTO();

        // Thông tin cơ bản
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setOrderNote(order.getOrderNote());

        // Thông tin khách hàng
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        }
        dto.setGuestEmail(order.getGuestEmail());

        // Thông tin người nhận (đã copy vào Order)
        dto.setShippingRecipientName(order.getShippingRecipientName());
        dto.setShippingRecipientPhone(order.getShippingRecipientPhone());
        dto.setShippingStreetAddress(order.getShippingStreetAddress());
        dto.setShippingWard(order.getShippingWard());
        dto.setShippingDistrict(order.getShippingDistrict());
        dto.setShippingCity(order.getShippingCity());

        // Thông tin vận chuyển
        if (order.getShippingMethod() != null) {
            dto.setShippingMethodName(order.getShippingMethod().getName());
        } else {
            dto.setShippingMethodName("N/A");
        }
        dto.setShippingCost(order.getShippingCost());

        // Thông tin khuyến mãi
        if (order.getPromotion() != null) {
            dto.setPromotionCode(order.getPromotion().getCode());
        }
        dto.setDiscountAmount(order.getDiscountAmount());

        // Chi tiết sản phẩm
        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .map(this::mapToOrderItemSummaryDTO)
                    .collect(Collectors.toList()));
            // Tính subtotal từ các item đã lưu trong đơn hàng
            dto.setSubtotal(order.getOrderItems().stream()
                    .filter(item -> item.getPriceAtPurchase() != null && item.getQuantity() != null)
                    .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            dto.setItems(Collections.emptyList());
            dto.setSubtotal(BigDecimal.ZERO);
        }

        // Tổng tiền cuối cùng
        dto.setTotalAmount(order.getTotalAmount());

        // TODO: Thêm lịch sử trạng thái nếu có bảng riêng

        return dto;
    }

    /**
     * Map Order entity sang UserOrderDetailDTO (cho chi tiết đơn hàng user).
     */
    private UserOrderDetailDTO mapToUserOrderDetailDTO(Order order) {
        if (order == null) return null;
        UserOrderDetailDTO dto = new UserOrderDetailDTO();

        // Copy các trường tương tự như AdminOrderDetailDTO, nhưng có thể lược bớt nếu user không cần xem
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setOrderNote(order.getOrderNote());

        dto.setShippingRecipientName(order.getShippingRecipientName());
        dto.setShippingRecipientPhone(order.getShippingRecipientPhone());
        dto.setShippingStreetAddress(order.getShippingStreetAddress());
        dto.setShippingWard(order.getShippingWard());
        dto.setShippingDistrict(order.getShippingDistrict());
        dto.setShippingCity(order.getShippingCity());

        if (order.getShippingMethod() != null) {
            dto.setShippingMethodName(order.getShippingMethod().getName());
        }
        dto.setShippingCost(order.getShippingCost());

        if (order.getPromotion() != null) {
            dto.setPromotionCode(order.getPromotion().getCode());
        }
        dto.setDiscountAmount(order.getDiscountAmount());

        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .map(this::mapToOrderItemSummaryDTO)
                    .collect(Collectors.toList()));
            dto.setSubtotal(order.getOrderItems().stream()
                    .filter(item -> item.getPriceAtPurchase() != null && item.getQuantity() != null)
                    .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            dto.setItems(Collections.emptyList());
            dto.setSubtotal(BigDecimal.ZERO);
        }
        dto.setTotalAmount(order.getTotalAmount());

        // Có thể thêm trackingUrl nếu có
        // dto.setTrackingUrl(...);

        return dto;
    }


    /**
     * Kiểm tra việc chuyển trạng thái đơn hàng có hợp lệ không.
     *
     * @param currentStatus Trạng thái hiện tại.
     * @param newStatus     Trạng thái mới.
     * @return true nếu hợp lệ, false nếu không.
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        // Không cho phép chuyển về trạng thái cũ
        if (currentStatus.equalsIgnoreCase(newStatus)) return false; // Sửa: không cho phép tự chuyển

        // Định nghĩa các luồng chuyển hợp lệ (CASE-INSENSITIVE)
        // Cần điều chỉnh dựa trên quy trình thực tế của bạn
        switch (currentStatus.toUpperCase()) {
            case "PENDING": // Chờ xử lý
            case "PAYMENT_PENDING": // Chờ thanh toán
                // Có thể chuyển sang Đang xử lý (nếu đã thanh toán hoặc COD), hoặc Hủy
                return List.of("PROCESSING", "CANCELLED").contains(newStatus.toUpperCase());
            case "PROCESSING": // Đang xử lý
                // Có thể chuyển sang Đang giao hàng, hoặc Hủy (nếu chưa giao)
                return List.of("SHIPPING", "CANCELLED").contains(newStatus.toUpperCase());
            case "SHIPPING": // Đang giao hàng
                // Có thể chuyển sang Đã giao hàng (Hoàn thành), hoặc Hủy (khó, tùy chính sách)
                return List.of("COMPLETED", "CANCELLED").contains(newStatus.toUpperCase());
            case "COMPLETED": // Đã giao hàng (Hoàn thành)
            case "CANCELLED": // Đã hủy
                // Không cho phép chuyển từ trạng thái cuối cùng
                return false;
            default:
                logger.warn("Encountered unknown current order status for transition check: {}", currentStatus);
                return false; // Trạng thái hiện tại không xác định -> không cho chuyển
        }
    }

    /**
     * Helper định dạng trạng thái sang tiếng Việt (có thể dùng chung từ formatters).
     */
    private String formatStatus(String status) {
        if (!StringUtils.hasText(status)) return "N/A";
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Chờ xử lý";
            case "PROCESSING":
                return "Đang xử lý";
            case "SHIPPING":
                return "Đang giao";
            case "COMPLETED":
                return "Đã giao"; // Hoặc "Hoàn thành"
            case "CANCELLED":
                return "Đã hủy";
            case "PAYMENT_PENDING":
                return "Chờ thanh toán";
            default:
                return status;
        }
    }
}