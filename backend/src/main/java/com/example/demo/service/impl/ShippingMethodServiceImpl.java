package com.example.demo.service.impl;

import com.example.demo.dto.shipping.ShippingMethodDTO;
import com.example.demo.entity.ShippingMethod;
import com.example.demo.exception.ResourceNotFoundException; // Import exception
import com.example.demo.repository.ShippingMethodRepository;
import com.example.demo.service.ShippingMethodService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException; // Import để check unique name
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // Đặt transactional ở mức class
public class ShippingMethodServiceImpl implements ShippingMethodService {

    private static final Logger logger = LoggerFactory.getLogger(ShippingMethodServiceImpl.class);
    private final ShippingMethodRepository shippingMethodRepository;

    // --- User Facing ---
    @Override
    @Transactional(readOnly = true)
    public List<ShippingMethodDTO> getActiveShippingMethods() {
        logger.debug("Fetching active shipping methods for user.");
        List<ShippingMethod> methods = shippingMethodRepository.findByIsActiveTrue();
        return methods.stream()
                .map(this::mapToShippingMethodDTO)
                .collect(Collectors.toList());
    }

    // --- Admin Facing ---
    @Override
    @Transactional(readOnly = true)
    public Page<ShippingMethodDTO> getAllShippingMethodsAdmin(Pageable pageable) {
        logger.info("ADMIN: Fetching all shipping methods with pageable: {}", pageable);
        Page<ShippingMethod> methodsPage = shippingMethodRepository.findAll(pageable);
        return methodsPage.map(this::mapToShippingMethodDTO); // Map sang DTO
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingMethodDTO getShippingMethodByIdAdmin(Integer id) {
        logger.info("ADMIN: Fetching shipping method by ID: {}", id);
        ShippingMethod method = findShippingMethodByIdOrThrow(id);
        return mapToShippingMethodDTO(method);
    }

    @Override
    @Transactional
    public ShippingMethodDTO createShippingMethod(ShippingMethodDTO dto) {
        logger.info("ADMIN: Creating new shipping method with name: {}", dto.getName());
        // Kiểm tra tên duy nhất (có thể DB đã xử lý nhưng check trước sẽ rõ lỗi hơn)
        if (shippingMethodRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
            throw new DataIntegrityViolationException("Shipping method name '" + dto.getName() + "' already exists.");
        }

        ShippingMethod newMethod = new ShippingMethod();
        BeanUtils.copyProperties(dto, newMethod, "id", "estimatedDelivery"); // Bỏ qua id và trường hiển thị
        ShippingMethod savedMethod = shippingMethodRepository.save(newMethod);
        logger.info("ADMIN: Shipping method created successfully with ID: {}", savedMethod.getId());
        return mapToShippingMethodDTO(savedMethod);
    }

    @Override
    @Transactional
    public ShippingMethodDTO updateShippingMethod(Integer id, ShippingMethodDTO dto) {
        logger.info("ADMIN: Updating shipping method with ID: {}", id);
        ShippingMethod existingMethod = findShippingMethodByIdOrThrow(id);

        // Kiểm tra tên duy nhất nếu tên thay đổi
        if (!existingMethod.getName().equalsIgnoreCase(dto.getName())) {
            shippingMethodRepository.findByNameIgnoreCase(dto.getName()).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) { // Đảm bảo không phải chính nó
                    throw new DataIntegrityViolationException("Shipping method name '" + dto.getName() + "' already exists.");
                }
            });
        }

        BeanUtils.copyProperties(dto, existingMethod, "id", "estimatedDelivery"); // Bỏ qua id và trường hiển thị
        ShippingMethod updatedMethod = shippingMethodRepository.save(existingMethod);
        logger.info("ADMIN: Shipping method updated successfully with ID: {}", updatedMethod.getId());
        return mapToShippingMethodDTO(updatedMethod);
    }

    @Override
    @Transactional
    public void deleteShippingMethod(Integer id) {
        logger.warn("ADMIN: Attempting to soft delete shipping method with ID: {}", id);
        ShippingMethod method = findShippingMethodByIdOrThrow(id);
        // Thực hiện soft delete bằng cách set isActive = false
        if (method.getIsActive()) { // Chỉ cập nhật nếu đang active
            method.setIsActive(false);
            shippingMethodRepository.save(method);
            logger.info("ADMIN: Shipping method ID {} marked as inactive.", id);
        } else {
            logger.info("ADMIN: Shipping method ID {} is already inactive.", id);
        }
        // Lưu ý: Không thực sự xóa khỏi DB. Nếu muốn xóa hẳn thì dùng shippingMethodRepository.delete(method);
        // nhưng cần cẩn thận với các đơn hàng cũ đang tham chiếu đến PTVC này.
    }

    // --- Helper Methods ---
    private ShippingMethod findShippingMethodByIdOrThrow(Integer id) {
        return shippingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShippingMethod", "id", id));
    }

    // Helper map Entity sang DTO (Giữ nguyên hoặc cập nhật nếu cần)
    private ShippingMethodDTO mapToShippingMethodDTO(ShippingMethod method) {
        if (method == null) return null;
        ShippingMethodDTO dto = new ShippingMethodDTO();
        BeanUtils.copyProperties(method, dto);
        // Tạo chuỗi mô tả thời gian giao hàng
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
}