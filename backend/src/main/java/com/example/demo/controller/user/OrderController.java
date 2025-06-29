// src/main/java/com/example/demo/controller/OrderController.java
package com.example.demo.controller.user;

import com.example.demo.dto.order.OrderListDTO;
import com.example.demo.dto.order.OrderSummaryDTO;
import com.example.demo.dto.order.UserOrderDetailDTO; // Import DTO chi tiết người dùng
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders") // Base path cho các API liên quan đến đơn hàng của người dùng
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Endpoint: GET /api/orders
     * Lấy danh sách các đơn hàng của người dùng đang đăng nhập, có hỗ trợ phân trang và sắp xếp.
     *
     * @param authentication Đối tượng chứa thông tin xác thực của người dùng.
     * @param pageable       Đối tượng chứa thông tin phân trang và sắp xếp (mặc định 10 mục/trang, sắp xếp theo ngày đặt hàng giảm dần).
     * @return ResponseEntity chứa một trang (Page) các OrderListDTO.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Yêu cầu người dùng phải đăng nhập
    public ResponseEntity<Page<OrderListDTO>> getUserOrders(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication.getName();
        Page<OrderListDTO> orderPage = orderService.getOrdersForUser(username, pageable);
        return ResponseEntity.ok(orderPage);
    }

    /**
     * Endpoint: GET /api/orders/{orderId}
     * Lấy chi tiết một đơn hàng cụ thể thuộc về người dùng đang đăng nhập.
     *
     * @param orderId        ID của đơn hàng cần lấy chi tiết (lấy từ URL path).
     * @param authentication Đối tượng chứa thông tin xác thực của người dùng.
     * @return ResponseEntity chứa UserOrderDetailDTO của đơn hàng nếu tìm thấy và hợp lệ.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserOrderDetailDTO> getUserOrderDetail(
            @PathVariable Integer orderId,
            Authentication authentication) {
        String username = authentication.getName();
        // Giả sử OrderService có phương thức getUserOrderDetail(Integer orderId, String username)
        UserOrderDetailDTO orderDetail = orderService.getUserOrderDetail(orderId, username);
        return ResponseEntity.ok(orderDetail);
    }

    /**
     * Endpoint: GET /api/orders/{orderId}/summary
     * Lấy thông tin tóm tắt của một đơn hàng cụ thể thuộc về người dùng đang đăng nhập.
     * (Có thể không cần thiết nếu đã có endpoint lấy chi tiết đầy đủ ở trên,
     * nhưng giữ lại nếu vẫn cần dùng ở trang Success hoặc nơi khác cần ít thông tin hơn).
     *
     * @param orderId        ID của đơn hàng cần lấy tóm tắt (lấy từ URL path).
     * @param authentication Đối tượng chứa thông tin xác thực của người dùng.
     * @return ResponseEntity chứa OrderSummaryDTO của đơn hàng nếu tìm thấy và hợp lệ.
     */
    @GetMapping("/{orderId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderSummaryDTO> getOrderSummary(
            @PathVariable Integer orderId,
            Authentication authentication) {
        String username = authentication.getName();
        OrderSummaryDTO summary = orderService.getOrderSummaryForUser(orderId, username);
        return ResponseEntity.ok(summary);
    }

    // Ghi chú: Có thể thêm các endpoint khác sau này nếu cần:
    // - POST /api/orders/{orderId}/cancel: Endpoint để user hủy đơn hàng (nếu được phép).
    // - API cho Admin (nên tách ra controller riêng hoặc dùng prefix /api/admin/orders):
    //   - GET /api/admin/orders: Xem tất cả đơn hàng (cần @PreAuthorize("hasRole('ADMIN')")).
    //   - GET /api/admin/orders/{orderId}: Xem chi tiết đơn hàng bất kỳ.
    //   - PUT /api/admin/orders/{orderId}/status: Cập nhật trạng thái đơn hàng.
}