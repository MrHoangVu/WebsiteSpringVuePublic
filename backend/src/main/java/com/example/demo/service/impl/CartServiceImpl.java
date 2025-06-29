// src/main/java/com/example/demo/service/impl/CartServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.cart.AddItemRequestDTO;
import com.example.demo.dto.cart.CartDTO;
import com.example.demo.dto.cart.CartItemDTO;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService; // Interface CartService cần được định nghĩa hoặc tạo mới
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Mặc định các phương thức public là transactional
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Lấy hoặc tạo giỏ hàng cho người dùng hoặc khách.
     * Ưu tiên tìm theo userId, sau đó đến sessionId.
     * Nếu không tìm thấy, tạo mới.
     *
     * @param username      Username (có thể null nếu là guest).
     * @param cartSessionId Session ID của guest (có thể null nếu user đăng nhập).
     * @return Đối tượng Cart (đảm bảo không null).
     */
    @Override
    public Cart getOrCreateCart(String username, String cartSessionId) {
        Integer userId = null;
        User user = null;

        if (username != null) {
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            userId = user.getId();
        }

        Optional<Cart> existingCart;
        if (userId != null) {
            existingCart = cartRepository.findByUserId(userId);
        } else if (cartSessionId != null && !cartSessionId.isBlank()) {
            existingCart = cartRepository.findBySessionId(cartSessionId);
        } else {
            // Trường hợp không có username và không có sessionId hợp lệ
            // -> không thể xác định giỏ hàng -> ném lỗi hoặc tạo cart không có định danh?
            // Tạm thời ném lỗi nếu là guest mà không có sessionId
            if (username == null) {
                logger.warn("Guest access attempt without a valid Cart Session ID.");
                throw new BadRequestException("Cart Session ID is required for guest cart access.");
            }
            // Nếu user đăng nhập mà chưa có cart (hiếm), ta sẽ tạo ở dưới
            existingCart = Optional.empty();
        }


        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            // Đảm bảo cart của guest không bị gắn nhầm user và ngược lại
            if (user != null && cart.getUser() == null && cart.getSessionId() != null) {
                logger.info("Associating guest cart ID {} with user ID {}", cart.getId(), userId);
                cart.setUser(user);
                cart.setSessionId(null); // Xóa sessionId khi đã gán user
                return cartRepository.save(cart);
            }
            if (user == null && cart.getUser() != null) {
                // Trường hợp này rất lạ, user đã logout nhưng cart vẫn còn user_id?
                // Có thể cần clear user_id nếu logic logout chưa xử lý?
                logger.warn("Cart ID {} still associated with user ID {} but accessed by guest.", cart.getId(), cart.getUser().getId());
                // Tạm thời vẫn trả về cart này
            }
            logger.debug("Found existing cart ID: {}", cart.getId());
            return cart;
        } else {
            // Nếu không tìm thấy cart và user đã đăng nhập (hoặc là guest có session hợp lệ) -> Tạo mới
            logger.info("Creating new cart for user: {} / session: {}", username, cartSessionId);
            Cart newCart = new Cart();
            newCart.setUser(user); // user có thể null
            newCart.setSessionId(userId == null ? cartSessionId : null); // Chỉ lưu sessionId cho guest
            return cartRepository.save(newCart);
        }
    }

    /**
     * Lấy thông tin chi tiết giỏ hàng dưới dạng DTO.
     *
     * @param username      Username (có thể null).
     * @param cartSessionId Session ID (có thể null).
     * @return CartDTO chứa thông tin giỏ hàng.
     */
    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartDto(String username, String cartSessionId) {
        Cart cart = getOrCreateCart(username, cartSessionId);
        CartDTO cartDTO = new CartDTO(cart.getId());

        List<CartItemDTO> itemDTOs = cart.getCartItems().stream()
                .map(this::mapToCartItemDTO) // Sử dụng mapToCartItemDTO mới
                .collect(Collectors.toList());

        cartDTO.setItems(itemDTOs);
        cartDTO.calculateTotals(); // Tính tổng số lượng và tổng tiền

        return cartDTO;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng hoặc cập nhật số lượng nếu đã tồn tại.
     *
     * @param username      Username (có thể null).
     * @param cartSessionId Session ID (có thể null).
     * @param request       DTO chứa productId và quantity.
     * @return CartDTO sau khi thêm/cập nhật.
     */
    @Override
    public CartDTO addItemToCart(String username, String cartSessionId, AddItemRequestDTO request) {
        Cart cart = getOrCreateCart(username, cartSessionId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Kiểm tra sản phẩm có active không
        if (product.getIsActive() == null || !product.getIsActive()) {
            throw new BadRequestException("Product '" + product.getName() + "' is currently unavailable.");
        }

        int quantityToAdd = request.getQuantity();
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            int newQuantity = cartItem.getQuantity() + quantityToAdd;
            logger.debug("Product ID {} already in cart ID {}. Updating quantity from {} to {}", request.getProductId(), cart.getId(), cartItem.getQuantity(), newQuantity);
            // Kiểm tra tồn kho với số lượng MỚI
            if (product.getStock() == null || product.getStock() < newQuantity) {
                throw new BadRequestException(String.format(
                        "sản phẩm '%s' đã hết hàng.",
                        product.getName()
                ));

            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem); // Lưu cập nhật item
        } else {
            // Kiểm tra tồn kho với số lượng cần thêm
            if (product.getStock() == null || product.getStock() < quantityToAdd) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + (product.getStock() != null ? product.getStock() : 0));
            }
            logger.debug("Adding new product ID {} with quantity {} to cart ID {}.", request.getProductId(), quantityToAdd, cart.getId());
            cartItem = new CartItem();
            cartItem.setCart(cart); // Quan trọng: Set cart cho item
            cartItem.setProduct(product);
            cartItem.setQuantity(quantityToAdd);
            cart.getCartItems().add(cartItem); // Thêm vào set trong Cart entity
            cartItemRepository.save(cartItem); // Lưu item mới
            // Hoặc nếu Cart có cascade persist thì chỉ cần save(cart)
            // cartRepository.save(cart);
        }

        return getCartDto(username, cartSessionId); // Trả về DTO giỏ hàng mới nhất
    }

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ hàng.
     * Nếu số lượng <= 0, sản phẩm sẽ bị xóa.
     *
     * @param username      Username (có thể null).
     * @param cartSessionId Session ID (có thể null).
     * @param productId     ID sản phẩm cần cập nhật.
     * @param newQuantity   Số lượng mới.
     * @return CartDTO sau khi cập nhật.
     */
    @Override
    public CartDTO updateItemQuantity(String username, String cartSessionId, Integer productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItemFromCart(username, cartSessionId, productId); // Gọi hàm xóa nếu quantity <= 0
        }

        Cart cart = getOrCreateCart(username, cartSessionId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (product.getIsActive() == null || !product.getIsActive()) {
            throw new BadRequestException("Product '" + product.getName() + "' is currently unavailable.");
        }
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ID " + productId + " in Cart", cart.getId()));

        // Kiểm tra tồn kho
        if (product.getStock() == null || product.getStock() < newQuantity) {
            throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + (product.getStock() != null ? product.getStock() : 0));
        }

        logger.debug("Updating quantity for product ID {} in cart ID {} to {}", productId, cart.getId(), newQuantity);
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return getCartDto(username, cartSessionId);
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng.
     *
     * @param username      Username (có thể null).
     * @param cartSessionId Session ID (có thể null).
     * @param productId     ID sản phẩm cần xóa.
     * @return CartDTO sau khi xóa.
     */
    @Override
    public CartDTO removeItemFromCart(String username, String cartSessionId, Integer productId) {
        Cart cart = getOrCreateCart(username, cartSessionId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId)); // Quan trọng: Check product tồn tại

        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            logger.info("Removing product ID {} from cart ID {}", productId, cart.getId());
            cart.getCartItems().remove(cartItem); // Xóa khỏi collection trong Cart (quan trọng cho orphanRemoval)
            cartItemRepository.delete(cartItem); // Xóa trực tiếp khỏi DB
            // cartRepository.save(cart); // Có thể cần nếu không dùng orphanRemoval hoặc để cập nhật updatedAt
        } else {
            logger.warn("Product ID {} not found in cart ID {} for removal.", productId, cart.getId());
            // Không ném lỗi nếu item không có sẵn, chỉ log lại
        }

        return getCartDto(username, cartSessionId);
    }

    /**
     * Xóa tất cả sản phẩm khỏi giỏ hàng.
     *
     * @param username      Username (có thể null).
     * @param cartSessionId Session ID (có thể null).
     * @return CartDTO rỗng.
     */
    @Override
    public CartDTO clearCart(String username, String cartSessionId) {
        Cart cart = getOrCreateCart(username, cartSessionId);
        logger.info("Clearing all items from cart ID: {}", cart.getId());
        if (!cart.getCartItems().isEmpty()) {
            cart.getCartItems().clear(); // Xóa khỏi collection (kích hoạt orphanRemoval nếu có)
            cartItemRepository.deleteByCart(cart); // Xóa trực tiếp để đảm bảo
            // cartRepository.save(cart); // Cập nhật updatedAt
        }
        // Tạo CartDTO rỗng để trả về
        CartDTO emptyCartDTO = new CartDTO(cart.getId());
        emptyCartDTO.calculateTotals();
        return emptyCartDTO;
    }

    /**
     * Gộp giỏ hàng của khách vào giỏ hàng của người dùng khi đăng nhập.
     *
     * @param guestCartSessionId Session ID của giỏ hàng khách.
     * @param username           Username của người dùng vừa đăng nhập.
     * @return CartDTO cuối cùng của người dùng sau khi gộp.
     */
    @Override
    public CartDTO mergeGuestCartToUserCart(String guestCartSessionId, String username) {
        if (guestCartSessionId == null || guestCartSessionId.isBlank() || username == null) {
            logger.warn("Cannot merge cart with null/blank sessionId or null username.");
            return getCartDto(username, null); // Trả về cart hiện tại của user
        }

        logger.info("Attempting to merge guest cart (session: {}) into user cart (username: {})", guestCartSessionId, username);

        Optional<Cart> guestCartOpt = cartRepository.findBySessionId(guestCartSessionId);
        if (guestCartOpt.isEmpty()) {
            logger.info("No guest cart found for session ID: {}. No merge needed.", guestCartSessionId);
            return getCartDto(username, null); // Trả về cart của user
        }

        Cart guestCart = guestCartOpt.get();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        Cart userCart = getOrCreateCart(username, null); // Lấy hoặc tạo cart cho user

        // Ngăn việc gộp giỏ hàng vào chính nó
        if (guestCart.getId().equals(userCart.getId())) {
            logger.warn("Guest cart and user cart are the same (ID: {}). Skipping merge, ensuring user association.", userCart.getId());
            if (userCart.getUser() == null) {
                userCart.setUser(user);
                userCart.setSessionId(null);
                cartRepository.save(userCart);
            }
            return getCartDto(username, null);
        }

        logger.info("Merging items from guest cart ID {} to user cart ID {}", guestCart.getId(), userCart.getId());

        // Tạo bản sao list item để tránh ConcurrentModificationException
        List<CartItem> itemsToMerge = new java.util.ArrayList<>(guestCart.getCartItems());
        guestCart.getCartItems().clear(); // Xóa reference khỏi guest cart

        for (CartItem guestItem : itemsToMerge) {
            Product product = guestItem.getProduct();
            // Bỏ qua nếu product không tồn tại hoặc không active
            if (product == null || product.getIsActive() == null || !product.getIsActive()) {
                logger.warn("Skipping merge for item with product ID {} (product not found or inactive).", guestItem.getProduct() != null ? guestItem.getProduct().getId() : "unknown");
                // Quan trọng: Xóa item này khỏi DB vì nó không hợp lệ nữa
                cartItemRepository.delete(guestItem);
                continue;
            }

            Optional<CartItem> userItemOpt = cartItemRepository.findByCartAndProduct(userCart, product);
            int quantityToMerge = guestItem.getQuantity();

            if (userItemOpt.isPresent()) {
                // Sản phẩm đã có trong giỏ user -> cộng dồn số lượng
                CartItem userItem = userItemOpt.get();
                int combinedQuantity = userItem.getQuantity() + quantityToMerge;
                logger.debug("Product ID {} exists in user cart. Merging quantity: {} + {} = {}", product.getId(), userItem.getQuantity(), quantityToMerge, combinedQuantity);
                // Kiểm tra lại tồn kho với số lượng tổng
                if (product.getStock() == null || product.getStock() < combinedQuantity) {
                    logger.warn("Insufficient stock after merging for product ID {}. Available: {}. Setting quantity to max available.", product.getId(), product.getStock());
                    userItem.setQuantity(product.getStock() != null ? product.getStock() : 0); // Set số lượng tối đa có thể
                } else {
                    userItem.setQuantity(combinedQuantity);
                }
                cartItemRepository.save(userItem); // Lưu thay đổi số lượng
                cartItemRepository.delete(guestItem); // Xóa item từ guest cart cũ trong DB

            } else {
                logger.debug("Product ID {} does not exist in user cart. Moving item from guest cart.", product.getId());
                // Sản phẩm chưa có -> Chuyển item từ guest sang user cart
                // Kiểm tra tồn kho trước khi chuyển
                if (product.getStock() == null || product.getStock() < quantityToMerge) {
                    logger.warn("Insufficient stock for product ID {} from guest cart. Available: {}. Skipping item.", product.getId(), product.getStock());
                    cartItemRepository.delete(guestItem); // Xóa item không hợp lệ
                    continue;
                }
                guestItem.setCart(userCart); // QUAN TRỌNG: Gán item cho userCart
                userCart.getCartItems().add(guestItem); // Thêm vào collection của userCart
                cartItemRepository.save(guestItem); // Lưu item với cart_id mới
            }
        }

        // Lưu userCart để cập nhật updatedAt (nếu cần)
        cartRepository.save(userCart);

        // Xóa guest cart cũ khỏi DB sau khi đã xử lý xong items
        try {
            logger.info("Deleting guest cart ID: {}", guestCart.getId());
            cartRepository.delete(guestCart);
        } catch (Exception e) {
            // Log lỗi nhưng không nên ném ra làm rollback việc merge
            logger.error("Error deleting guest cart ID: {}", guestCart.getId(), e);
        }

        return getCartDto(username, null); // Trả về cart đã merge của user
    }


    // --- Helper Methods ---

    // Helper map CartItem entity sang DTO (Cập nhật)
    private CartItemDTO mapToCartItemDTO(CartItem item) {
        Product product = item.getProduct();
        if (product == null) {
            logger.error("CartItem ID {} refers to a non-existent Product. This item should be removed.", item.getId());
            // Cân nhắc xóa item lỗi này khỏi DB ở đây hoặc trong một job dọn dẹp
            // cartItemRepository.delete(item); // Cẩn thận khi xóa trong hàm readOnly
            return null; // Hoặc trả về DTO báo lỗi
        }
        BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemDTO(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getImageUrl(),
                item.getQuantity(),
                price, // Giá đơn vị HIỆN TẠI của sản phẩm
                lineTotal
        );
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public BigDecimal calculateSubtotalForCart(String username, String cartSessionId) {
        logger.debug("Calculating subtotal for user: '{}', session: '{}'", username, cartSessionId);
        Cart cart = getOrCreateCart(username, cartSessionId); // Lấy cart hiện tại
        BigDecimal total = BigDecimal.ZERO;

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            logger.debug("Cart ID {} is empty, subtotal is ZERO.", cart.getId());
            return total; // Trả về 0 nếu giỏ hàng trống
        }

        // Tạo bản sao để tránh ConcurrentModificationException nếu có logic xóa item lỗi
        // Đồng thời fetch lại Product để đảm bảo giá là mới nhất và kiểm tra active
        List<CartItem> currentItems = new java.util.ArrayList<>(cart.getCartItems());
        List<Integer> productIds = currentItems.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        // Fetch các product tương ứng từ DB để lấy giá mới nhất và trạng thái active
        Map<Integer, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (CartItem item : currentItems) {
            Product product = productMap.get(item.getProduct().getId());
            // Kiểm tra sản phẩm tồn tại và active
            if (product != null && product.getIsActive() != null && product.getIsActive()) {
                BigDecimal currentPrice = product.getPrice(); // Lấy giá MỚI NHẤT từ DB
                if (currentPrice != null && item.getQuantity() != null && item.getQuantity() > 0) {
                    BigDecimal lineTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    total = total.add(lineTotal);
                    logger.trace("Item Product ID: {}, Qty: {}, Price: {}, LineTotal: {}, Current Cart Total: {}",
                            product.getId(), item.getQuantity(), currentPrice, lineTotal, total);
                } else {
                    logger.warn("Invalid price or quantity for CartItem ID {} (Product ID {}). Price: {}, Qty: {}. Skipping.",
                            item.getId(), product.getId(), currentPrice, item.getQuantity());
                }
            } else {
                logger.warn("Product ID {} not found, inactive, or has no price for CartItem ID {}. Excluding from subtotal.",
                        (product != null ? product.getId() : "unknown"), item.getId());
                // Cân nhắc xóa item lỗi này khỏi giỏ hàng nếu cần
            }
        }
        logger.debug("Calculated subtotal for Cart ID {}: {}", cart.getId(), total);
        return total;
    }
}
