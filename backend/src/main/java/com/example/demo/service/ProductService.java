// src/main/java/com/example/demo/service/ProductService.java
package com.example.demo.service;

import com.example.demo.dto.product.ProductCreateUpdateDTO;
import com.example.demo.dto.product.ProductDetailDTO;
import com.example.demo.dto.product.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface định nghĩa các dịch vụ liên quan đến quản lý và truy vấn sản phẩm.
 */
public interface ProductService {

    // --- User-facing methods ---

    /**
     * Tìm kiếm và lọc sản phẩm cho trang người dùng (chỉ trả về các sản phẩm đang hoạt động - isActive=true).
     * Hỗ trợ lọc theo từ khóa, danh mục (một hoặc nhiều), khoảng giá, chất liệu.
     *
     * @param keyword     Từ khóa tìm kiếm (có thể null).
     * @param categoryId  Lọc theo ID danh mục cụ thể (thường dùng khi điều hướng từ slug, có thể null).
     * @param categoryIds Danh sách ID danh mục để lọc (từ bộ lọc đa chọn, có thể null hoặc rỗng).
     * @param minPrice    Giá tối thiểu (có thể null).
     * @param maxPrice    Giá tối đa (có thể null).
     * @param material    Chất liệu cần lọc (có thể null).
     * @param pageable    Thông tin phân trang và sắp xếp.
     * @return Một trang (Page) chứa các ProductSummaryDTO tóm tắt của sản phẩm phù hợp.
     */
    Page<ProductSummaryDTO> findUserProducts(
            String keyword,
            Integer categoryId,
            List<Integer> categoryIds,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String material,
            Pageable pageable);

    /**
     * Lấy chi tiết một sản phẩm đang hoạt động (isActive=true) dựa trên slug của nó.
     *
     * @param slug Slug của sản phẩm cần tìm.
     * @return ProductDetailDTO chứa thông tin chi tiết sản phẩm.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu không tìm thấy sản phẩm active với slug tương ứng.
     */
    ProductDetailDTO findProductBySlug(String slug);

    // --- ADMIN METHODS ---

    /**
     * Lấy danh sách sản phẩm cho trang quản trị viên, hỗ trợ lọc và phân trang.
     * Có thể lấy cả sản phẩm đang hoạt động và ngừng bán.
     *
     * @param keyword    Từ khóa tìm kiếm (có thể null).
     * @param categoryId Lọc theo ID danh mục (có thể null).
     * @param isActive   Lọc theo trạng thái hoạt động (true, false, hoặc null để lấy tất cả).
     * @param pageable   Thông tin phân trang và sắp xếp.
     * @return Một trang (Page) chứa các ProductDetailDTO chi tiết của sản phẩm phù hợp.
     */
    Page<ProductDetailDTO> findAdminProducts(
            String keyword,
            Integer categoryId,
            Boolean isActive,
            Pageable pageable);

    /**
     * Tạo một sản phẩm mới.
     *
     * @param dto Dữ liệu sản phẩm mới từ request (ProductCreateUpdateDTO).
     * @return ProductDetailDTO của sản phẩm vừa được tạo.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu categoryId không hợp lệ.
     * @throws org.springframework.dao.DataIntegrityViolationException nếu slug hoặc các ràng buộc unique khác bị vi phạm.
     */
    ProductDetailDTO createProduct(ProductCreateUpdateDTO dto);

    /**
     * Cập nhật thông tin một sản phẩm hiện có.
     *
     * @param productId ID của sản phẩm cần cập nhật.
     * @param dto       Dữ liệu cập nhật từ request (ProductCreateUpdateDTO).
     * @return ProductDetailDTO của sản phẩm sau khi đã cập nhật.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu không tìm thấy sản phẩm hoặc categoryId mới không hợp lệ.
     * @throws org.springframework.dao.DataIntegrityViolationException nếu slug mới bị trùng.
     */
    ProductDetailDTO updateProduct(Integer productId, ProductCreateUpdateDTO dto);

    /**
     * Lấy chi tiết một sản phẩm (bất kể trạng thái active) cho trang quản trị viên.
     *
     * @param productId ID của sản phẩm cần lấy.
     * @return ProductDetailDTO chứa thông tin chi tiết.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu không tìm thấy sản phẩm với ID tương ứng.
     */
    ProductDetailDTO getProductByIdForAdmin(Integer productId);

    /**
     * Cập nhật trạng thái hoạt động (active/inactive) cho một sản phẩm.
     *
     * @param productId ID của sản phẩm cần cập nhật trạng thái.
     * @param isActive  Trạng thái mới (true = active, false = inactive).
     * @return ProductDetailDTO của sản phẩm sau khi đã cập nhật trạng thái.
     * @throws com.example.demo.exception.ResourceNotFoundException nếu không tìm thấy sản phẩm.
     */
    ProductDetailDTO setProductStatus(Integer productId, boolean isActive);

}