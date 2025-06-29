package com.example.demo.config;



import com.example.demo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter; // Đảm bảo filter chỉ chạy 1 lần/request

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy JWT token từ request header
            String jwt = getJwtFromRequest(request);

            // 2. Validate token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 3. Lấy username từ token
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // 4. Tải thông tin user (UserDetails) từ DB
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 5. Tạo đối tượng Authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 6. Đặt thông tin chi tiết xác thực (IP, session ID,...) vào đối tượng Authentication
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Quan trọng: Đặt đối tượng Authentication vào SecurityContext
                // -> Spring Security sẽ biết user này đã được xác thực
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Không ném lỗi ra ngoài để tránh lộ thông tin, chỉ log lại
        }

        // 8. Chuyển tiếp request tới filter tiếp theo trong chuỗi filter
        filterChain.doFilter(request, response);
    }

    // Helper method để lấy token từ Header "Authorization: Bearer <token>"
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ đi "Bearer "
        }
        return null;
    }
}