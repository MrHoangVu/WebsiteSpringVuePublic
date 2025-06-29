package com.example.demo.controller.admin;

import com.example.demo.dto.order.AdminOrderDetailDTO;
import com.example.demo.dto.order.AdminOrderListDTO;
import com.example.demo.dto.order.UpdateOrderStatusDTO;
import com.example.demo.entity.Order; // Import Order để nhận kết quả từ service
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/orders") // Base path cho admin orders
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller này
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    /**
     * Lấy danh sách tất cả đơn hàng với khả năng lọc, tìm kiếm và phân trang.
     *
     * @param keyword   Từ khóa tìm kiếm (mã đơn, tên/email/sdt khách hàng...).
     * @param status    Trạng thái đơn hàng cần lọc.
     * @param startDate Ngày bắt đầu lọc (YYYY-MM-DD).
     * @param endDate   Ngày kết thúc lọc (YYYY-MM-DD).
     * @param pageable  Thông tin phân trang và sắp xếp.
     * @return Trang kết quả chứa danh sách đơn hàng (AdminOrderListDTO).
     */
    @GetMapping
    public ResponseEntity<Page<AdminOrderListDTO>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 15, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        logger.info("GET /api/admin/orders - Fetching orders with filters: keyword='{}', status='{}', startDate={}, endDate={}, pageable={}",
                keyword, status, startDate, endDate, pageable);

        // Gọi service để lấy dữ liệu đã được lọc và phân trang
        Page<AdminOrderListDTO> orderPage = orderService.getAllOrders(keyword, status, startDate, endDate, pageable);

        // Trả về kết quả trong ResponseEntity
        return ResponseEntity.ok(orderPage);
    }

    /**
     * Lấy chi tiết một đơn hàng theo ID cho admin.
     *
     * @param orderId ID của đơn hàng cần xem.
     * @return Chi tiết đơn hàng (AdminOrderDetailDTO).
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDetailDTO> getOrderDetail(@PathVariable Integer orderId) {
        logger.info("GET /api/admin/orders/{} - Fetching order details", orderId);
        AdminOrderDetailDTO orderDetail = orderService.getOrderDetailForAdmin(orderId);
        return ResponseEntity.ok(orderDetail);
    }

    /**
     * Cập nhật trạng thái của một đơn hàng.
     *
     * @param orderId       ID của đơn hàng cần cập nhật.
     * @param statusUpdate  DTO chứa trạng thái mới.
     * @return Chi tiết đơn hàng (AdminOrderDetailDTO) sau khi đã cập nhật trạng thái.
     */
    @PutMapping("/{orderId}/status") // Hoặc @PatchMapping đều hợp lý
    public ResponseEntity<AdminOrderDetailDTO> updateOrderStatus(
            @PathVariable Integer orderId,
            @Valid @RequestBody UpdateOrderStatusDTO statusUpdate) {

        logger.info("PUT /api/admin/orders/{}/status - Updating status to '{}'", orderId, statusUpdate.getNewStatus());

        // 1. Gọi service để thực hiện cập nhật trạng thái
        // Service nên trả về entity Order đã được cập nhật để lấy ID
        Order updatedOrder = orderService.updateOrderStatus(orderId, statusUpdate.getNewStatus());

        // 2. Sau khi cập nhật thành công, gọi lại service để lấy DTO chi tiết mới nhất
        // Điều này đảm bảo dữ liệu trả về client là đầy đủ và đồng bộ nhất
        AdminOrderDetailDTO updatedOrderDetailDTO = orderService.getOrderDetailForAdmin(updatedOrder.getId());

        logger.info("PUT /api/admin/orders/{}/status - Status updated successfully", orderId);

        // 3. Trả về DTO chi tiết mới nhất cho client
        return ResponseEntity.ok(updatedOrderDetailDTO);
    }
}