// src/main/java/com/example/demo/service/impl/UserServiceImpl.java
package com.example.demo.service.impl; // Đảm bảo package name đúng

import com.example.demo.dto.admin.AdminUserDetailDTO;
import com.example.demo.dto.admin.AdminUserListDTO;
import com.example.demo.dto.order.OrderItemSummaryDTO;
import com.example.demo.dto.order.OrderListDTO;
import com.example.demo.dto.user.AddressDTO;
import com.example.demo.dto.user.ChangePasswordDTO;
import com.example.demo.dto.user.UpdateUserProfileDTO;
import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.entity.Address;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem; // Import OrderItem nếu dùng trong mapToOrderListDTO
import com.example.demo.entity.Product; // Import Product nếu dùng trong mapToOrderListDTO
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService; // Import interface UserService
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service // Đánh dấu là Service Bean
@RequiredArgsConstructor // Tự động inject các final fields qua constructor
@Transactional // Mặc định transaction cho các public method
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // --- Dependencies ---
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    // --- Ngưỡng chi tiêu cho các bậc (Ví dụ) ---
    private static final BigDecimal GOLD_TIER_THRESHOLD = new BigDecimal("10000000"); // 10 triệu
    private static final BigDecimal SILVER_TIER_THRESHOLD = new BigDecimal("3000000"); // 3 triệu

    // --- ADMIN METHODS ---

    /**
     * Lấy danh sách khách hàng cho trang quản trị viên với bộ lọc và phân trang.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListDTO> getCustomerList(String keyword, String tier, Boolean isActive, Pageable pageable) {
        logger.info("Fetching customer list for admin. Keyword: '{}', Tier: {}, IsActive: {}, Pageable: {}", keyword, tier, isActive, pageable);
        // Gọi phương thức repository đã tạo (sử dụng Specification hoặc query trực tiếp)
        // Giả sử đã có findCustomers trong UserRepository
        Page<User> userPage = userRepository.findCustomers(keyword, tier, isActive, pageable);
        return userPage.map(this::mapToAdminUserListDTO); // Map sang DTO list
    }

    /**
     * Lấy chi tiết một khách hàng cho trang quản trị viên.
     */
    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailDTO getCustomerDetail(Integer userId) {
        logger.info("Fetching customer detail for admin. User ID: {}", userId);
        User user = userRepository.findByIdAndRole(userId, "CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", userId));

        AdminUserDetailDTO dto = mapToAdminUserDetailDTO(user);

        List<Address> addresses = addressRepository.findByUserId(userId);
        dto.setAddresses(addresses.stream().map(this::mapToAddressDTO).collect(Collectors.toList()));

        // Lấy 5 đơn hàng gần nhất - Tạo Pageable KHÔNG CÓ SORT
        // Pageable recentOrdersPageable = PageRequest.of(0, 5); // Chỉ cần trang và kích thước
        // Hoặc rõ ràng hơn với Sort.unsorted()
        Pageable recentOrdersPageable = PageRequest.of(0, 5, Sort.unsorted()); // <<< SỬA Ở ĐÂY

        // Giữ nguyên tên phương thức repository
        Page<Order> recentOrdersPage = orderRepository.findByUserIdOrderByOrderDateDesc(userId, recentOrdersPageable);
        dto.setRecentOrders(recentOrdersPage.getContent().stream().map(this::mapToOrderListDTO).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Cập nhật bậc (tier) của khách hàng bởi Admin.
     */
    @Override
    @Transactional
    public User updateCustomerTier(Integer userId, String newTier) {
        logger.info("Admin updating tier for user ID: {} to {}", userId, newTier);
        User user = userRepository.findByIdAndRole(userId, "CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", userId));

        // Validate newTier (có thể dùng Enum)
        String upperTier = newTier.toUpperCase();
        if (!List.of("BRONZE", "SILVER", "GOLD", "DIAMOND").contains(upperTier)) {
            throw new BadRequestException("Bậc khách hàng không hợp lệ: " + newTier);
        }

        user.setTier(upperTier);
        User updatedUser = userRepository.save(user);
        logger.info("User ID {} tier updated successfully to {}", userId, updatedUser.getTier());
        return updatedUser; // Trả về entity User đã cập nhật
    }

    /**
     * Cập nhật trạng thái hoạt động (khóa/mở khóa) của khách hàng bởi Admin.
     */
    @Override
    @Transactional
    public User updateCustomerStatus(Integer userId, boolean isActive) {
        logger.info("Admin updating status for user ID: {} to {}", userId, isActive ? "Active" : "Inactive");
        User user = userRepository.findByIdAndRole(userId, "CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", userId));

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);
        logger.info("User ID {} status updated successfully to {}", userId, updatedUser.getIsActive());
        return updatedUser; // Trả về entity User đã cập nhật
    }

    /**
     * Tính toán và cập nhật tổng chi tiêu và bậc cho khách hàng (thường gọi sau khi đơn hàng hoàn thành).
     */
    @Override
    @Transactional
    public void updateTotalSpentAndTier(Integer userId, BigDecimal orderAmount) {
        if (userId == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid input for updateTotalSpentAndTier. UserID: {}, Amount: {}", userId, orderAmount);
            return;
        }
        logger.debug("Updating total spent and tier for user ID: {} after order completion (amount: {})", userId, orderAmount);
        // Dùng findById vì có thể áp dụng cho user không phải CUSTOMER nếu logic thay đổi
        User user = userRepository.findById(userId).orElse(null);

        if (user != null && "CUSTOMER".equals(user.getRole())) { // Chỉ áp dụng cho CUSTOMER
            BigDecimal currentTotalSpent = user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO;
            BigDecimal newTotalSpent = currentTotalSpent.add(orderAmount);
            user.setTotalSpent(newTotalSpent);

            // Tính toán bậc mới
            String newTier = calculateTierBasedOnSpending(newTotalSpent);
            if (!newTier.equals(user.getTier())) {
                logger.info("User ID {} tier changed from {} to {} based on total spent {}", userId, user.getTier(), newTier, newTotalSpent);
                user.setTier(newTier);
            } else {
                logger.debug("User ID {} tier remains {} with total spent {}", userId, user.getTier(), newTotalSpent);
            }
            userRepository.save(user); // Lưu lại thay đổi (totalSpent và có thể cả tier)
        } else {
            logger.warn("User ID {} not found or not a customer, cannot update spending.", userId);
        }
    }

    // --- USER PROFILE METHODS ---

    /**
     * Lấy thông tin chi tiết của người dùng đang đăng nhập.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile(String username) {
        logger.debug("Fetching profile for user: {}", username);
        User user = findUserByUsernameOrThrowInternal(username); // Dùng helper nội bộ
        return mapToUserProfileDTO(user);
    }

    /**
     * Cập nhật thông tin cơ bản của người dùng đang đăng nhập.
     */
    @Override
    @Transactional
    public UserProfileDTO updateUserProfile(String username, UpdateUserProfileDTO dto) {
        logger.info("Updating profile for user: {}", username);
        User user = findUserByUsernameOrThrowInternal(username);

        boolean updated = false;
        // Chỉ cập nhật fullName nếu DTO có giá trị và khác giá trị hiện tại
        if (StringUtils.hasText(dto.getFullName()) && !dto.getFullName().equals(user.getFullName())) {
            user.setFullName(dto.getFullName());
            updated = true;
            logger.debug("Updating fullName for user {}", username);
        }
        // Thêm logic cập nhật email nếu có
        // if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())) {
        //     // TODO: Kiểm tra email tồn tại, gửi email xác thực...
        //     user.setEmail(dto.getEmail());
        //     updated = true;
        //     logger.debug("Updating email for user {}", username);
        // }

        if (updated) {
            User savedUser = userRepository.save(user);
            logger.info("Profile updated successfully for user: {}", username);
            return mapToUserProfileDTO(savedUser);
        } else {
            logger.info("No changes detected for user profile: {}", username);
            return mapToUserProfileDTO(user);
        }
    }

    /**
     * Thay đổi mật khẩu cho người dùng đang đăng nhập.
     */
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        logger.info("Attempting to change password for user: {}", username);
        User user = findUserByUsernameOrThrowInternal(username);

        // 1. Xác thực mật khẩu cũ
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            logger.warn("Password change failed for user {}: Incorrect current password.", username);
            throw new BadRequestException("Mật khẩu hiện tại không chính xác.");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận khớp nhau (đã được xử lý bởi @AssertTrue trong DTO)
        // if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
        //     throw new BadRequestException("Mật khẩu mới và mật khẩu xác nhận không khớp.");
        // }

        // 3. Kiểm tra mật khẩu mới có trùng mật khẩu cũ không
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        // 4. Mã hóa và cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", username);
    }


    // --- HELPER MAPPER (Public cho AdminUserController) ---

    /**
     * Map User entity sang AdminUserListDTO.
     */
    @Override // Đảm bảo có @Override
    public AdminUserListDTO mapToAdminUserListDTO(User user) {
        if (user == null) return null;
        AdminUserListDTO dto = new AdminUserListDTO();
        BeanUtils.copyProperties(user, dto);
        // Gán các trường khác nếu cần
        return dto;
    }


    // --- PRIVATE HELPER METHODS ---

    /**
     * Helper nội bộ để tìm User theo username hoặc ném ResourceNotFoundException.
     *
     * @param username Tên đăng nhập.
     * @return User entity.
     */
    private User findUserByUsernameOrThrowInternal(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    /**
     * Helper map User entity sang UserProfileDTO.
     */
    private UserProfileDTO mapToUserProfileDTO(User user) {
        if (user == null) return null;
        UserProfileDTO dto = new UserProfileDTO();
        BeanUtils.copyProperties(user, dto);
        // Gán thêm các trường không khớp tên nếu cần
        return dto;
    }

    /**
     * Helper map User entity sang AdminUserDetailDTO (chỉ thông tin cơ bản).
     * Addresses và Orders sẽ được thêm sau trong getCustomerDetail.
     */
    private AdminUserDetailDTO mapToAdminUserDetailDTO(User user) {
        if (user == null) return null;
        AdminUserDetailDTO dto = new AdminUserDetailDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setAddresses(Collections.emptyList()); // Khởi tạo list rỗng
        dto.setRecentOrders(Collections.emptyList());
        return dto;
    }


    /**
     * Helper map Address entity sang AddressDTO.
     */
    private AddressDTO mapToAddressDTO(Address address) {
        if (address == null) return null;
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    /**
     * Helper map Order entity sang OrderListDTO (tóm tắt).
     */
    private OrderListDTO mapToOrderListDTO(Order order) {
        if (order == null) return null;
        OrderListDTO dto = new OrderListDTO();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        // Lấy vài item đầu tiên làm preview
        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .limit(3) // Giới hạn số lượng item preview
                    .map(this::mapToOrderItemSummaryDTO) // Gọi helper map item
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    /**
     * Helper map OrderItem entity sang OrderItemSummaryDTO.
     */
    private OrderItemSummaryDTO mapToOrderItemSummaryDTO(OrderItem item) {
        if (item == null) return null;
        OrderItemSummaryDTO itemDto = new OrderItemSummaryDTO();
        itemDto.setQuantity(item.getQuantity());
        itemDto.setPriceAtPurchase(item.getPriceAtPurchase());
        Product product = item.getProduct(); // Lấy product từ item
        if (product != null) {
            itemDto.setProductId(product.getId());
            itemDto.setProductName(product.getName());
            itemDto.setProductImageUrl(product.getImageUrl());
        } else {
            // Xử lý trường hợp product bị null (ví dụ: sản phẩm đã bị xóa)
            itemDto.setProductName("Sản phẩm không còn tồn tại");
        }
        return itemDto;
    }


    /**
     * Helper tính toán bậc khách hàng dựa trên tổng chi tiêu.
     *
     * @param totalSpent Tổng số tiền đã chi tiêu.
     * @return Mã bậc khách hàng (String).
     */
    private String calculateTierBasedOnSpending(BigDecimal totalSpent) {
        if (totalSpent == null) return "BRONZE";
        // So sánh bằng compareTo cho BigDecimal
        if (totalSpent.compareTo(GOLD_TIER_THRESHOLD) >= 0) {
            return "GOLD";
        } else if (totalSpent.compareTo(SILVER_TIER_THRESHOLD) >= 0) {
            return "SILVER";
        } else {
            return "BRONZE";
        }
        // Thêm bậc DIAMOND nếu cần
    }
}