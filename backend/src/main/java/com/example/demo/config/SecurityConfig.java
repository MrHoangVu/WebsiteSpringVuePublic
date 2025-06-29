// src/main/java/com/example/demo/config/SecurityConfig.java
package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Bật nếu bạn dùng @PreAuthorize nhiều trong các Controller/Service
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import static org.springframework.security.config.Customizer.withDefaults; // Dùng cho cors(withDefaults())

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity // Bật annotation này nếu bạn muốn dùng @PreAuthorize thay vì cấu hình tập trung ở đây
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì dùng JWT (stateless)
                .cors(withDefaults()) // Sử dụng cấu hình CORS mặc định (cần bean CorsConfigurationSource nếu muốn tùy chỉnh)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API không dùng session
                .authenticationProvider(authenticationProvider()) // Khai báo AuthenticationProvider
                .authorizeHttpRequests(authorize -> authorize
                        // --- Public Routes (Không cần đăng nhập) ---
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**",
                                "/api/products/**",                     // Xem sản phẩm, danh mục, chi tiết, tìm kiếm...
                                "/api/products/{productId:\\d+}/reviews", // Xem reviews của sản phẩm
                                "/api/articles", "/api/articles/**",    // Xem danh sách và chi tiết bài viết
                                "/api/shipping-methods/active"          // Lấy các phương thức vận chuyển đang hoạt động (cho cả guest)
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/register", // Đăng nhập, đăng ký
                                "/api/cart", "/api/cart/items",         // Tạo cart mới, thêm item vào cart (cho cả guest)
                                "/api/checkout/place-order"             // Cho phép Guest đặt hàng
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT,
                                "/api/cart/items/**"                    // Cập nhật số lượng item trong cart (cho cả guest)
                        ).permitAll()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/cart", "/api/cart/items/**"       // Xóa cart hoặc xóa item khỏi cart (cho cả guest)
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",                      // Tài liệu OpenAPI v3
                                "/swagger-ui/**",                       // Giao diện Swagger UI
                                "/swagger-ui.html"                      // Trang Swagger UI
                        ).permitAll()

                        // --- Authenticated Routes (Cần đăng nhập, không phân biệt Role) ---
                        .requestMatchers(HttpMethod.POST,
                                "/api/products/{productId:\\d+}/reviews", // Gửi review mới
                                "/api/articles/create"                  // Tạo bài viết mới (nếu user thường được phép)
                                // Thêm các endpoint POST/PUT/DELETE khác của user cần login
                        ).authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/users/me",                       // Lấy thông tin user đang đăng nhập
                                "/api/orders", "/api/orders/**",       // Lấy danh sách và chi tiết đơn hàng của user
                                "/api/checkout/info"                   // Lấy thông tin đã lưu cho checkout (địa chỉ...)
                                // Thêm các endpoint GET khác của user cần login
                        ).authenticated()
                        // Các API checkout khác (nếu có) mà không phải place-order public
                        // .requestMatchers("/api/checkout/calculate-fee").authenticated() // Ví dụ

                        // --- Admin Routes (Yêu cầu Role ADMIN) ---
                        // Cách 1: Gom chung (nếu tất cả API admin đều bắt đầu bằng /api/admin)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        /*
                        // Cách 2: Khai báo chi tiết (nếu cần phân quyền admin khác nhau hoặc path không đồng nhất)
                        .requestMatchers("/api/admin/stats/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/promotions/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/articles/**").hasRole("ADMIN")
                        */

                        // --- Default ---
                        .anyRequest().authenticated() // Tất cả các request còn lại chưa được khai báo ở trên đều yêu cầu đăng nhập
                )
                // Thêm JWT filter vào trước filter xử lý username/password mặc định
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}