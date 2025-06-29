// src/main/java/com/example/demo/service/CheckoutService.java
package com.example.demo.service;

import com.example.demo.dto.checkout.CheckoutInfoDTO;
import com.example.demo.dto.checkout.PlaceOrderRequestDTO;
import com.example.demo.dto.order.OrderSummaryDTO;

public interface CheckoutService {
    CheckoutInfoDTO getCheckoutInfo(String username);
    OrderSummaryDTO placeOrder(PlaceOrderRequestDTO request, String username);

}