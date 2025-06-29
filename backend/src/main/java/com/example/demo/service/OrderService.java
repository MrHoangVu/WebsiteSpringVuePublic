// src/main/java/com/example/demo/service/OrderService.java
package com.example.demo.service;

import com.example.demo.dto.order.AdminOrderDetailDTO;
import com.example.demo.dto.order.AdminOrderListDTO;
import com.example.demo.dto.order.OrderListDTO;
import com.example.demo.dto.order.OrderSummaryDTO;
import com.example.demo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Import Pageable
import com.example.demo.dto.order.UserOrderDetailDTO;

import java.time.LocalDate;

public interface OrderService {
    // User methods
    OrderSummaryDTO getOrderSummaryForUser(Integer orderId, String username);
    Page<OrderListDTO> getOrdersForUser(String username, Pageable pageable);
    UserOrderDetailDTO getUserOrderDetail(Integer orderId, String username); // <<< Method mới

    // Admin methods
    Page<AdminOrderListDTO> getAllOrders(String keyword, String status, LocalDate startDate, LocalDate endDate, Pageable pageable); // <<< SỬA CHỮ KÝ

    AdminOrderDetailDTO getOrderDetailForAdmin(Integer orderId);
    Order updateOrderStatus(Integer orderId, String newStatus);
}