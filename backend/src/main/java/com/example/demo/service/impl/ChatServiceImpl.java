package com.example.demo.service.impl;

import com.example.demo.entity.*; // Import các entity cần thiết
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*; // Import các repository cần thiết
import com.example.demo.repository.specification.ArticleSpecifications;
import com.example.demo.repository.specification.ProductSpecifications; // Cần import này
import com.example.demo.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest; // Import cho phân trang
import org.springframework.data.domain.Pageable;    // Import cho phân trang
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final String SYSTEM_INSTRUCTIONS = """
            Bạn là trợ lý ảo của website Tượng Gỗ Phong Thủy HV. Nhiệm vụ của bạn là trả lời các câu hỏi của khách hàng một cách lịch sự, hữu ích và tập trung vào các sản phẩm và thông tin có trên website.
            Thông tin cốt lõi về website:
            - Chuyên cung cấp: Tượng gỗ mỹ nghệ phong thủy (chủ yếu từ gỗ Hương, Trắc, Mun, Pơ Mu, Nu Nghiến) và vật phẩm phong thủy bằng đá tự nhiên.
            - Các loại tượng gỗ phổ biến: Tượng Di Lặc (ý nghĩa: tài lộc, vui vẻ, hạnh phúc), Tượng Quan Công (ý nghĩa: bảo vệ, trấn trạch, lòng trung thành, dũng cảm), Tượng Phật Bà Quan Âm (ý nghĩa: từ bi, bình an, cứu khổ), Tượng Đạt Ma (ý nghĩa: giác ngộ, thiền định).
            - Vật phẩm phong thủy khác: Tỳ Hưu (chiêu tài, giữ lộc), Thiềm Thừ/Cóc Ngậm Tiền (may mắn tiền bạc).
            - Chất liệu gỗ: Đảm bảo gỗ tự nhiên, chất lượng cao.
            - Chất liệu đá: Đá tự nhiên (Ngọc Pakistan, Thạch Anh,...).
            - Chính sách: Thông tin chi tiết về vận chuyển, đổi trả, bảo hành có tại các trang Chính sách trên website. Phí vận chuyển sẽ được thông báo cụ thể khi khách hàng tiến hành đặt hàng hoặc liên hệ trực tiếp.
            - Liên hệ: Nếu cần tư vấn chi tiết hơn, xem sản phẩm trực tiếp hoặc đặt hàng theo yêu cầu, khách hàng có thể gọi hotline 0987654321 hoặc nhắn tin qua Zalo (số điện thoại 0987654321). Giờ làm việc: T2-T7 (8h-18h).

            Quy tắc trả lời:
            1. Luôn lịch sự, thân thiện và chuyên nghiệp.
            2. Ưu tiên trả lời dựa trên thông tin được cung cấp trong phần "CONTEXT DATABASE" nếu có. Nếu không có context hoặc context không đủ, hãy dựa vào kiến thức chung và thông tin cốt lõi ở trên.
            3. KHÔNG được bịa đặt thông tin sản phẩm (giá cả chính xác, tình trạng tồn kho chính xác) nếu không có trong context. Hãy hướng dẫn khách xem chi tiết trên trang sản phẩm hoặc liên hệ cửa hàng nếu context không có.
            4. KHÔNG được tự ý nhận đặt hàng hoặc báo giá cuối cùng qua chat. Hãy hướng dẫn khách đặt hàng trên website hoặc liên hệ hotline/Zalo.
            5. Nếu câu hỏi nằm ngoài phạm vi (ví dụ: hỏi về chính trị, tôn giáo khác, thông tin cá nhân nhạy cảm), hãy lịch sự từ chối trả lời và lái về chủ đề của website.
            6. Nếu không chắc chắn về câu trả lời (kể cả khi có context), hãy nói rằng bạn cần kiểm tra thêm hoặc gợi ý khách hàng liên hệ trực tiếp để được tư vấn chính xác nhất.
            7. Giữ câu trả lời tương đối ngắn gọn, dễ hiểu. Tránh lặp lại thông tin đã có trong câu hỏi.
            8. Nếu người trả lời tự xưng là "Hoàng Vũ Phong Thủy",hãy trả lời mọi câu hỏi(kể cả ngoài phạm vi) .
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository; // Đảm bảo đã inject CategoryRepository

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    private static final int MAX_CONTEXT_LENGTH = 5500; // Giới hạn ký tự cho context DB
    private static final Locale VIETNAM_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(VIETNAM_LOCALE);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy", VIETNAM_LOCALE);

    // --- Patterns for Intent Recognition ---
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("(?:đơn hàng|mã đơn|order)\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("(?:sản phẩm|tượng|sp|giá của|thông tin)\\s+([\\p{L}\\s\\d]+)(?:\\s+là gì|\\s+bao nhiêu|\\s+thế nào|\\?)?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ARTICLE_TITLE_PATTERN = Pattern.compile("(?:bài viết|tin tức|hướng dẫn|thông tin về)\\s+([\\p{L}\\s\\d]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern MY_ORDER_PATTERN = Pattern.compile("(?:đơn hàng của tôi|đơn của tôi|my order)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // --- Patterns for Enhanced Product Search ---
    private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile("(?:giá|khoảng|từ)\\s*(\\d+(?:[.,]\\d+)*)\\s*(?:triệu|tr)?\\s*(?:đến|-)\\s*(\\d+(?:[.,]\\d+)*)\\s*(?:triệu|tr)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile("(?:giá|trên|từ)\\s*(\\d+(?:[.,]\\d+)*)\\s*(?:triệu|tr)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile("(?:giá|dưới|đến)\\s*(\\d+(?:[.,]\\d+)*)\\s*(?:triệu|tr)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("(?:loại|dòng|kiểu)?\\s*(tượng di lặc|di lặc|quan công|phật bà|tỳ hưu|thiềm thừ)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE); // Cập nhật pattern category


    @Override
    public String getGeminiResponse(String userMessage, List<Map<String, Object>> history) {
        logger.info("Processing chat message: User='{}', History size={}", userMessage, history != null ? history.size() : 0);

        if (!StringUtils.hasText(userMessage)) {
            return "Vui lòng nhập câu hỏi của bạn.";
        }
        if (!isValidApiConfig()) {
            return "Lỗi cấu hình: Không thể kết nối đến dịch vụ chat.";
        }

        String dbContext = retrieveContextData(userMessage);
        String finalPrompt = buildGeminiPrompt(userMessage, dbContext, history);

        return callGeminiApi(finalPrompt, history);
    }

    // ================================================
    // === RAG - Context Retrieval ====================
    // ================================================

    private String retrieveContextData(String userMessage) {
        String context = "";
        String lowerUserMessage = userMessage.toLowerCase();

        // --- Ưu tiên các intent cụ thể trước ---
        Matcher orderMatcher = ORDER_ID_PATTERN.matcher(userMessage);
        if (orderMatcher.find()) {
            try {
                Integer orderId = Integer.parseInt(orderMatcher.group(1));
                logger.debug("Intent: Order Inquiry. Extracted Order ID: {}", orderId);
                context = getOrderContext(orderId);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse order ID from message: {}", userMessage);
            }
            return context; // Trả về sớm nếu đã xử lý intent cụ thể
        }

        if (MY_ORDER_PATTERN.matcher(lowerUserMessage).find()) {
            logger.debug("Intent: My Orders Inquiry.");
            context = getMyOrdersContext();
            return context;
        }

        Matcher articleMatcher = ARTICLE_TITLE_PATTERN.matcher(userMessage);
        if (articleMatcher.find()) {
            String articleTitleOrKeyword = articleMatcher.group(1).trim();
            logger.debug("Intent: Article Inquiry. Extracted Title/Keyword: {}", articleTitleOrKeyword);
            context = getArticleContext(articleTitleOrKeyword);
            return context;
        }

        // --- Xử lý intent tìm kiếm sản phẩm phức tạp ---
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Integer categoryId = null;
        String productNameKeyword = null; // Keyword chung nếu không xác định được loại cụ thể

        // Trích xuất khoảng giá
        Matcher rangeMatcher = PRICE_RANGE_PATTERN.matcher(userMessage);
        if (rangeMatcher.find()) {
            try {
                BigDecimal multiplier = new BigDecimal("1000000"); // Giả định đơn vị là triệu
                minPrice = parsePrice(rangeMatcher.group(1)).multiply(multiplier);
                maxPrice = parsePrice(rangeMatcher.group(2)).multiply(multiplier);
                logger.debug("Extracted Price Range: {} - {}", CURRENCY_FORMATTER.format(minPrice), CURRENCY_FORMATTER.format(maxPrice));
            } catch (NumberFormatException e) {
                logger.warn("Could not parse price range from: {}", userMessage);
            }
        } else {
            // Trích xuất giá min/max riêng lẻ nếu không có khoảng
            Matcher minMatcher = MIN_PRICE_PATTERN.matcher(userMessage);
            if (minMatcher.find()) {
                try {
                    minPrice = parsePrice(minMatcher.group(1)).multiply(new BigDecimal("1000000"));
                    logger.debug("Extracted Min Price: {}", CURRENCY_FORMATTER.format(minPrice));
                } catch (NumberFormatException e) { /* ignore */ }
            }
            Matcher maxMatcher = MAX_PRICE_PATTERN.matcher(userMessage);
            if (maxMatcher.find()) {
                try {
                    maxPrice = parsePrice(maxMatcher.group(1)).multiply(new BigDecimal("1000000"));
                    logger.debug("Extracted Max Price: {}", CURRENCY_FORMATTER.format(maxPrice));
                } catch (NumberFormatException e) { /* ignore */ }
            }
        }

        // Trích xuất loại sản phẩm (Category)
        Matcher categoryMatcher = CATEGORY_PATTERN.matcher(lowerUserMessage);
        if (categoryMatcher.find()) {
            String categoryKeyword = categoryMatcher.group(1).trim(); // Lấy group chứa tên loại
            logger.debug("Extracted Category Keyword: {}", categoryKeyword);
            // *** Giả định bạn có phương thức này trong CategoryRepository ***
            Optional<Category> categoryOpt = categoryRepository.findFirstByNameContainingIgnoreCase(categoryKeyword);
            if(categoryOpt.isPresent()){
                categoryId = categoryOpt.get().getId();
                logger.debug("Found Category ID: {}", categoryId);
            } else {
                logger.warn("Category keyword '{}' found but no matching category in DB.", categoryKeyword);
                productNameKeyword = categoryKeyword; // Dùng keyword này để tìm tên SP nếu ko thấy category
            }
        }

        // Nếu không tìm thấy loại cụ thể, thử trích xuất keyword chung từ pattern sản phẩm
        if (categoryId == null && productNameKeyword == null) {
            Matcher productMatcher = PRODUCT_NAME_PATTERN.matcher(userMessage);
            if (productMatcher.find()) {
                productNameKeyword = productMatcher.group(1).trim();
                logger.debug("Extracted General Product Keyword: {}", productNameKeyword);
            }
        }

        // --- Thực hiện truy vấn DB với các tiêu chí đã trích xuất ---
        if (categoryId != null || minPrice != null || maxPrice != null || productNameKeyword != null) {
            logger.info("Intent: Complex Product Search. CategoryId: {}, MinPrice: {}, MaxPrice: {}, Keyword: {}", categoryId, minPrice, maxPrice, productNameKeyword);
            context = findAndFormatMultipleProducts(categoryId, minPrice, maxPrice, productNameKeyword);
        } else {
            // Nếu không có tiêu chí nào rõ ràng, có thể trả lời chung chung hoặc không tìm context DB
            logger.info("No specific product search criteria extracted. May rely on general knowledge.");
            // Không trả về context nào, để AI tự xử lý câu hỏi chung
        }

        // Log context cuối cùng trước khi trả về
        if (!context.isEmpty()) {
            logger.info("Retrieved DB Context ({} chars)", context.length());
            // logger.info("Retrieved DB Context ({} chars): {}", context.length(), context); // Bỏ log nội dung context để tránh quá dài
        } else {
            logger.info("No relevant DB context found for the query.");
        }
        return context;
    }

    /**
     * Helper parse giá (xử lý dấu phẩy/chấm và loại bỏ ký tự không phải số).
     */
    private BigDecimal parsePrice(String priceString) throws NumberFormatException {
        if (priceString == null) throw new NumberFormatException("Price string is null");
        // Loại bỏ dấu chấm ngăn cách hàng nghìn, thay dấu phẩy thập phân bằng dấu chấm
        String cleanedPrice = priceString.replace(".", "").replace(",", ".");
        // Loại bỏ các ký tự không phải số hoặc dấu chấm thập phân duy nhất (nếu có)
        cleanedPrice = cleanedPrice.replaceAll("[^\\d.]", "");
        // Đảm bảo chỉ có một dấu chấm thập phân (lấy dấu chấm cuối cùng nếu có nhiều)
        int lastDot = cleanedPrice.lastIndexOf('.');
        if (lastDot != -1) {
            cleanedPrice = cleanedPrice.substring(0, lastDot).replace(".", "") + cleanedPrice.substring(lastDot);
        }

        if (cleanedPrice.isEmpty()) throw new NumberFormatException("Cleaned price string is empty");
        return new BigDecimal(cleanedPrice);
    }

    /**
     * Tìm kiếm sản phẩm theo nhiều tiêu chí và định dạng context.
     * Giả định ProductSpecifications.filterByCriteria đã được tạo.
     */
    private String findAndFormatMultipleProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        // *** Giả định bạn có phương thức này trong ProductSpecifications ***
        Specification<Product> spec = ProductSpecifications.filterByCriteria(categoryId, minPrice, maxPrice, keyword);

        // Lấy tối đa 3-5 sản phẩm phù hợp để đưa vào context
        Pageable pageable = PageRequest.of(0, 3); // Lấy top 3
        List<Product> products = productRepository.findAll(spec, pageable).getContent();

        if (products.isEmpty()) {
            // Xây dựng thông báo không tìm thấy dựa trên tiêu chí
            List<String> criteria = new ArrayList<>();
            if (categoryId != null) {
                // *** Giả định bạn có CategoryRepository để lấy tên ***
                criteria.add("loại " + categoryRepository.findById(categoryId).map(Category::getName).orElse("đã chọn"));
            }
            if (minPrice != null && maxPrice != null) criteria.add("giá từ " + CURRENCY_FORMATTER.format(minPrice) + " đến " + CURRENCY_FORMATTER.format(maxPrice));
            else if (minPrice != null) criteria.add("giá từ " + CURRENCY_FORMATTER.format(minPrice));
            else if (maxPrice != null) criteria.add("giá dưới " + CURRENCY_FORMATTER.format(maxPrice));
            if (StringUtils.hasText(keyword)) criteria.add("từ khóa '" + keyword + "'");

            if (!criteria.isEmpty()) {
                return "Xin lỗi, cửa hàng hiện không có sản phẩm nào phù hợp với tiêu chí: " + String.join(" và ", criteria) + ".";
            } else {
                return "Xin lỗi, không tìm thấy sản phẩm nào phù hợp."; // Trường hợp rất hiếm
            }
        } else {
            return formatMultipleProductsContext(products, categoryId, minPrice, maxPrice, keyword);
        }
    }

    /**
     * Định dạng context cho nhiều sản phẩm tìm thấy.
     */
    private String formatMultipleProductsContext(List<Product> products, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword) {
        StringBuilder contextBuilder = new StringBuilder();
        // Tạo câu dẫn dựa trên tiêu chí tìm kiếm
        List<String> criteriaDesc = new ArrayList<>();
        if (categoryId != null) {
            // *** Giả định bạn có CategoryRepository để lấy tên ***
            criteriaDesc.add("loại " + categoryRepository.findById(categoryId).map(Category::getName).orElse("đã chọn"));
        }
        if (minPrice != null && maxPrice != null) criteriaDesc.add("giá từ " + CURRENCY_FORMATTER.format(minPrice) + " đến " + CURRENCY_FORMATTER.format(maxPrice));
        else if (minPrice != null) criteriaDesc.add("giá từ " + CURRENCY_FORMATTER.format(minPrice));
        else if (maxPrice != null) criteriaDesc.add("giá dưới " + CURRENCY_FORMATTER.format(maxPrice));
        if (StringUtils.hasText(keyword)) criteriaDesc.add("chứa '" + keyword + "'");

        if (!criteriaDesc.isEmpty()) {
            contextBuilder.append("Tìm thấy ").append(products.size()).append(" sản phẩm phù hợp với tiêu chí (").append(String.join(", ", criteriaDesc)).append("):\n");
        } else {
            contextBuilder.append("Tìm thấy ").append(products.size()).append(" sản phẩm phù hợp:\n");
        }

        for (Product product : products) {
            contextBuilder.append(String.format("- %s: %s (%s). %s (%d có sẵn).\n",
                    product.getName(),
                    CURRENCY_FORMATTER.format(product.getPrice()),
                    product.getMaterial() != null ? product.getMaterial() : "N/A",
                    product.getStock() > 0 ? "Còn hàng" : "Hết hàng",
                    product.getStock()
            ));
            // Có thể thêm mô tả ngắn nếu cần và độ dài cho phép
            // String desc = product.getDescription() != null ? product.getDescription().substring(0, Math.min(50, product.getDescription().length())) + "..." : "";
            // contextBuilder.append("  Mô tả: ").append(desc).append("\n");
        }
        // Giới hạn độ dài tổng thể
        String context = contextBuilder.toString();
        return context.substring(0, Math.min(context.length(), MAX_CONTEXT_LENGTH));
    }


    // ================================================
    // === RAG - Existing Context Helpers =============
    // ================================================

    private String getOrderContext(Integer orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Cần kiểm tra quyền truy cập nếu cần thiết
            // if (!hasPermissionToViewOrder(order)) { return "Bạn không có quyền xem đơn hàng này."; }
            return formatOrderContext(order);
        } else {
            return String.format("Không tìm thấy đơn hàng với mã #%d.", orderId);
        }
    }

    private String getMyOrdersContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "Vui lòng đăng nhập để xem đơn hàng của bạn.";
        }
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Authenticated user '{}' not found in DB for 'my orders' query.", username);
            return "Không tìm thấy thông tin tài khoản của bạn.";
        }
        Integer userId = userOpt.get().getId();

        List<Order> recentOrders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, PageRequest.of(0, 3)).getContent();

        if (recentOrders.isEmpty()) {
            return "Bạn chưa có đơn hàng nào gần đây.";
        }

        StringBuilder contextBuilder = new StringBuilder("Đây là các đơn hàng gần đây của bạn:\n");
        for (Order order : recentOrders) {
            contextBuilder.append(String.format("- Đơn #%d (%s): %s, Tổng tiền: %s\n",
                    order.getId(),
                    order.getOrderDate().format(DATE_TIME_FORMATTER),
                    formatOrderStatus(order.getStatus()),
                    CURRENCY_FORMATTER.format(order.getTotalAmount())
            ));
        }
        return contextBuilder.toString();
    }

    /**
     * Lấy context cho một tên/keyword sản phẩm đơn giản (fallback hoặc khi không có tiêu chí phức tạp).
     * Ưu tiên tìm theo slug trước, sau đó tìm theo tên chứa keyword.
     */
    private String getProductContext(String nameOrKeyword) {
        String slug = Slugify.builder().build().slugify(nameOrKeyword);
        Optional<Product> productOpt = productRepository.findBySlugAndIsActiveTrue(slug);

        if (productOpt.isEmpty()) {
            List<Product> products = productRepository.searchActiveByNameOrDescriptionContainingIgnoreCase(nameOrKeyword, PageRequest.of(0, 1)).getContent();
            if (!products.isEmpty()) {
                productOpt = Optional.of(products.get(0));
            }
        }

        if (productOpt.isPresent()) {
            return formatProductContext(productOpt.get());
        } else {
            return String.format("Không tìm thấy sản phẩm nào phù hợp với '%s'. Bạn có thể tham khảo các sản phẩm khác trên website.", nameOrKeyword);
        }
    }

    private String getArticleContext(String titleOrKeyword) {
        String slug = Slugify.builder().build().slugify(titleOrKeyword);
        Optional<Article> articleOpt = articleRepository.findBySlugAndIsPublishedTrue(slug);

        if (articleOpt.isEmpty()) {
            // *** Giả định bạn có ArticleSpecifications.filterAdminArticles ***
            Specification<Article> spec = ArticleSpecifications.filterAdminArticles(titleOrKeyword, true);
            List<Article> articles = articleRepository.findAll(spec, PageRequest.of(0, 1)).getContent();
            if (!articles.isEmpty()) {
                articleOpt = Optional.of(articles.get(0));
            }
        }

        if (articleOpt.isPresent()) {
            return formatArticleContext(articleOpt.get());
        } else {
            return String.format("Không tìm thấy bài viết nào phù hợp với '%s'.", titleOrKeyword);
        }
    }

    private String formatOrderContext(Order order) {
        String itemsSummary = order.getOrderItems().stream()
                .map(item -> String.format("%s (SL: %d)", item.getProduct() != null ? item.getProduct().getName() : "SP đã xóa", item.getQuantity()))
                .collect(Collectors.joining(", "));
        String context = String.format("Đơn hàng #%d đặt lúc %s bởi %s. Trạng thái: %s. Tổng tiền: %s. Sản phẩm: %s.",
                order.getId(),
                order.getOrderDate().format(DATE_TIME_FORMATTER),
                order.getUser() != null ? order.getUser().getUsername() : (order.getGuestEmail() != null ? order.getGuestEmail() + " (Guest)" : "Khách"),
                formatOrderStatus(order.getStatus()),
                CURRENCY_FORMATTER.format(order.getTotalAmount()),
                itemsSummary
        );
        return context.substring(0, Math.min(context.length(), 1000)); // Giới hạn độ dài context đơn hàng
    }

    private String formatProductContext(Product product) {
        String descriptionExcerpt = product.getDescription() != null
                ? product.getDescription().substring(0, Math.min(product.getDescription().length(), 200)) + "..." // Rút gọn mô tả
                : "Chưa có mô tả.";
        String context = String.format("Sản phẩm '%s': Giá %s, Chất liệu: %s, Kích thước: %s. Tình trạng: %s (%d có sẵn). Mô tả ngắn: %s",
                product.getName(),
                CURRENCY_FORMATTER.format(product.getPrice()),
                product.getMaterial() != null ? product.getMaterial() : "Không rõ",
                product.getDimensions() != null ? product.getDimensions() : "Không rõ",
                product.getStock() > 0 ? "Còn hàng" : "Hết hàng",
                product.getStock(),
                descriptionExcerpt
        );
        return context.substring(0, Math.min(context.length(), 1000)); // Giới hạn độ dài context sản phẩm
    }

    private String formatArticleContext(Article article) {
        String contentExcerpt = article.getContent() != null
                ? article.getContent().replaceAll("<[^>]*>", "").substring(0, Math.min(article.getContent().replaceAll("<[^>]*>", "").length(), 300)) + "..." // Rút gọn nội dung, loại bỏ html tag
                : "Chưa có nội dung.";
        String context = String.format("Bài viết '%s' (Tác giả: %s, Ngày đăng: %s): %s",
                article.getTitle(),
                article.getUser() != null ? article.getUser().getUsername() : "Không rõ",
                article.getPublishedAt() != null ? article.getPublishedAt().format(DATE_TIME_FORMATTER) : "Chưa đăng",
                contentExcerpt
        );
        return context.substring(0, Math.min(context.length(), 1000)); // Giới hạn độ dài context bài viết
    }

    private String formatOrderStatus(String status) {
        if (!StringUtils.hasText(status)) return "Không rõ";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xử lý";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPING" -> "Đang giao";
            case "COMPLETED" -> "Đã giao";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_PENDING" -> "Chờ thanh toán";
            default -> status;
        };
    }

    // ================================================
    // === Prompt Building ============================
    // ================================================

    private String buildGeminiPrompt(String userMessage, String dbContext, List<Map<String, Object>> history) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(SYSTEM_INSTRUCTIONS).append("\n\n");

        if (StringUtils.hasText(dbContext)) {
            promptBuilder.append("--- CONTEXT DATABASE ---\n");
            // Đã giới hạn độ dài khi tạo context, không cần substring ở đây nữa
            promptBuilder.append(dbContext);
            promptBuilder.append("\n------------------------\n\n");
        } else {
            promptBuilder.append("--- CONTEXT DATABASE ---\n");
            promptBuilder.append("Không có thông tin cụ thể nào từ cơ sở dữ liệu cho câu hỏi này. Hãy trả lời dựa trên kiến thức chung về cửa hàng hoặc hỏi thêm thông tin nếu cần.");
            promptBuilder.append("\n------------------------\n\n");
        }

        // Thêm lịch sử chat (giữ nguyên logic cũ)
        if (history != null && !history.isEmpty()) {
            promptBuilder.append("--- HISTORY ---\n");
            // Giới hạn lịch sử để tránh prompt quá dài
            int historyLimit = 6; // Ví dụ: 3 cặp user/model gần nhất
            int startIndex = Math.max(0, history.size() - historyLimit);
            history.subList(startIndex, history.size()).forEach(msg -> {
                String role = (String) msg.getOrDefault("role", "unknown");
                // Xử lý cấu trúc parts an toàn hơn
                Object partsObj = msg.get("parts");
                String text = "";
                if (partsObj instanceof List<?> partsList && !partsList.isEmpty()) {
                    Object firstPartObj = partsList.get(0);
                    if (firstPartObj instanceof Map<?,?> partMap && partMap.get("text") instanceof String partText) {
                        text = partText;
                    }
                }
                // Chỉ thêm vào prompt nếu text không rỗng
                if (StringUtils.hasText(text)) {
                    promptBuilder.append(String.format("%s: %s\n", role.equals("model") ? "Bot" : "User", text));
                }
            });
            promptBuilder.append("---------------\n\n");
        }

        promptBuilder.append("Câu hỏi của khách hàng:\n\"").append(userMessage).append("\"\n\n");
        promptBuilder.append("Trả lời:");

        String finalPrompt = promptBuilder.toString();
        logger.trace("Final prompt being sent to Gemini:\n{}", finalPrompt);
        return finalPrompt;
    }


    // ================================================
    // === API Call and Parsing =======================
    // ================================================

    private String callGeminiApi(String finalPrompt, List<Map<String, Object>> history) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        // Xây dựng 'contents' cho API request
        // Thêm history (nếu có và hợp lệ)
        if (history != null && !history.isEmpty()) {
            int historyLimit = 6;
            int startIndex = Math.max(0, history.size() - historyLimit);
            // Lọc bỏ các message có text rỗng hoặc không đúng cấu trúc
            List<Map<String, Object>> validHistory = history.subList(startIndex, history.size()).stream()
                    .filter(msg -> {
                        Object partsObj = msg.get("parts");
                        if (partsObj instanceof List<?> partsList && !partsList.isEmpty()) {
                            Object firstPartObj = partsList.get(0);
                            return firstPartObj instanceof Map<?,?> partMap && partMap.get("text") instanceof String;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            contents.addAll(validHistory);
        }

        // Thêm prompt cuối cùng như một tin nhắn user mới
        // Lưu ý: Gemini có thể xử lý tốt hơn nếu system instructions và context được đặt riêng,
        // nhưng cách gộp vào tin nhắn cuối cùng này thường hoạt động.
        Map<String, Object> userMessagePart = new HashMap<>();
        userMessagePart.put("text", finalPrompt); // Gửi toàn bộ prompt đã xây dựng

        Map<String, Object> userMessageContent = new HashMap<>();
        userMessageContent.put("role", "user");
        userMessageContent.put("parts", List.of(userMessagePart));
        contents.add(userMessageContent);

        requestBody.put("contents", contents);

        // Thêm cấu hình an toàn (tùy chọn, điều chỉnh theo nhu cầu)
        // Map<String, Object> safetySetting = new HashMap<>();
        // safetySetting.put("category", "HARM_CATEGORY_HARASSMENT");
        // safetySetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        // requestBody.put("safetySettings", List.of(safetySetting));

        // Thêm cấu hình sinh text (tùy chọn)
        // Map<String, Object> generationConfig = new HashMap<>();
        // generationConfig.put("temperature", 0.7);
        // generationConfig.put("maxOutputTokens", 512);
        // requestBody.put("generationConfig", generationConfig);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String urlWithKey = apiUrl + "?key=" + apiKey;

        try {
            logger.trace("Sending final request body to Gemini API: {}", objectMapper.writeValueAsString(requestBody));
            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Received successful response from Gemini API.");
                return parseGeminiResponse(response.getBody());
            } else {
                logger.error("Gemini API request failed. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này (Lỗi API: " + response.getStatusCodeValue() + ").";
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP Error calling Gemini API. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            String detail = extractErrorMessage(e.getResponseBodyAsString());
            return "Xin lỗi, đã có lỗi xảy ra khi liên hệ dịch vụ chat" + detail + ". (Mã lỗi: " + e.getStatusCode().value() + ")";
        } catch (RestClientException e) {
            logger.error("RestClientException calling Gemini API: {}", e.getMessage(), e);
            return "Xin lỗi, không thể kết nối đến dịch vụ chat. Vui lòng kiểm tra lại kết nối mạng hoặc thử lại sau.";
        } catch (Exception e) {
            logger.error("Unexpected error processing Gemini request: {}", e.getMessage(), e);
            return "Xin lỗi, đã có lỗi hệ thống không mong muốn xảy ra.";
        }
    }

    private boolean isValidApiConfig() {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(apiUrl)) {
            logger.error("Gemini API Key or URL is not configured properly.");
            return false;
        }
        // Không nên kiểm tra key quá đơn giản như "YOUR_API_KEY_HERE" ở production
        if (apiKey.length() < 10) { // Kiểm tra độ dài tối thiểu cơ bản
            logger.warn("Potentially invalid API Key configured (too short). Please verify Gemini API Key.");
            // return false; // Có thể bỏ comment nếu muốn chặt chẽ hơn
        }
        return true;
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Kiểm tra promptFeedback trước (nếu có block)
            JsonNode promptFeedback = rootNode.path("promptFeedback");
            if (!promptFeedback.isMissingNode() && "BLOCK_REASON_SAFETY".equals(promptFeedback.path("blockReason").asText())) {
                logger.warn("Gemini request blocked due to prompt safety issues.");
                // Có thể lấy chi tiết hơn từ safetyRatings nếu cần
                return "Xin lỗi, yêu cầu của bạn không thể được xử lý do vấn đề an toàn nội dung trong câu hỏi.";
            }

            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.path(0);

                // Kiểm tra finishReason trước
                String finishReason = firstCandidate.path("finishReason").asText();
                if ("SAFETY".equalsIgnoreCase(finishReason)) {
                    logger.warn("Gemini response blocked due to SAFETY finish reason.");
                    // Có thể lấy chi tiết từ safetyRatings của candidate
                    return "Xin lỗi, phản hồi không thể hiển thị do vấn đề an toàn nội dung.";
                }
                if ("RECITATION".equalsIgnoreCase(finishReason)) {
                    logger.warn("Gemini response potentially blocked due to RECITATION.");
                    // Có thể cần xử lý khác
                }
                if ("MAX_TOKENS".equalsIgnoreCase(finishReason)) {
                    logger.info("Gemini response truncated due to MAX_TOKENS limit.");
                    // Phản hồi có thể bị cắt ngắn
                }
                // Các finishReason khác: STOP (bình thường), OTHER, UNSPECIFIED

                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    JsonNode firstPart = parts.path(0);
                    if (firstPart.has("text")) {
                        logger.trace("Successfully parsed text from Gemini response.");
                        return firstPart.path("text").asText();
                    }
                }
            }
            // Nếu không có candidates hoặc không parse được text
            logger.warn("Could not find expected text structure in Gemini response: {}", responseBody);
            return "Xin lỗi, phản hồi nhận được không đúng định dạng mong muốn hoặc bị chặn.";
        } catch (Exception e) {
            logger.error("Error parsing Gemini JSON response body: {}", e.getMessage(), e);
            return "Xin lỗi, có lỗi xảy ra khi đọc phản hồi từ dịch vụ chat.";
        }
    }

    private String extractErrorMessage(String errorBody) {
        if (!StringUtils.hasText(errorBody)) return "";
        try {
            JsonNode errorNode = objectMapper.readTree(errorBody);
            JsonNode errorMessageNode = errorNode.path("error").path("message");
            if (!errorMessageNode.isMissingNode()) {
                return ": " + errorMessageNode.asText();
            }
        } catch (Exception e) {
            logger.debug("Could not parse error JSON body: {}", errorBody, e);
        }
        // Trả về một phần của error body nếu không parse được JSON
        return ": " + errorBody.substring(0, Math.min(errorBody.length(), 100));
    }
}