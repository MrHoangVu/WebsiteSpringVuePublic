package com.example.demo.service;

import com.example.demo.dto.shipping.ShippingMethodDTO;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import java.util.List;

public interface ShippingMethodService {
    // --- User Facing ---
    List<ShippingMethodDTO> getActiveShippingMethods();

    // --- Admin Facing ---
    Page<ShippingMethodDTO> getAllShippingMethodsAdmin(Pageable pageable);
    ShippingMethodDTO getShippingMethodByIdAdmin(Integer id);
    ShippingMethodDTO createShippingMethod(ShippingMethodDTO dto);
    ShippingMethodDTO updateShippingMethod(Integer id, ShippingMethodDTO dto);
    void deleteShippingMethod(Integer id); // Soft delete
}