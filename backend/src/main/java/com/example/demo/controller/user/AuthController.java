package com.example.demo.controller.user;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.auth.AuthResponseDTO;
import com.example.demo.dto.auth.LoginRequestDTO;
import com.example.demo.dto.auth.RegisterRequestDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid; // Import validation annotation
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Exception cho sai credentials
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException; // Exception xác thực chung
import org.springframework.security.core.GrantedAuthority; // Để lấy role
import org.springframework.security.core.context.SecurityContextHolder; // Để lấy thông tin sau xác thực
import org.springframework.security.core.userdetails.UserDetails; // Để lấy username, roles
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Tiền tố chung cho API xác thực
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager; // Inject AuthenticationManager
    private final JwtTokenProvider tokenProvider; // Inject JwtTokenProvider
    private final AuthService authService; // Inject AuthService

    // API Endpoint: POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // 1. Thực hiện xác thực bằng AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Nếu xác thực thành công, đặt Authentication vào SecurityContext
            // (Mặc dù không bắt buộc cho việc tạo token, nhưng là thực hành tốt)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy thông tin UserDetails từ đối tượng Authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Lấy role từ authorities (giả sử chỉ có 1 role dạng ROLE_...)
            String role = userDetails.getAuthorities().stream()
                    .findFirst() // Lấy quyền đầu tiên
                    .map(GrantedAuthority::getAuthority) // Lấy tên quyền (VD: "ROLE_ADMIN")
                    .orElse("ROLE_USER"); // Mặc định là USER nếu không có quyền

            // 4. Tạo JWT token
            String jwt = tokenProvider.generateToken(authentication);
            // Hoặc: String jwt = tokenProvider.generateTokenFromUsername(username);

            // 5. Tạo và trả về response chứa token và thông tin user
            AuthResponseDTO authResponse = new AuthResponseDTO(jwt, "Bearer", username, role);
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException ex) {
            // 6. Xử lý nếu sai username hoặc password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401 Unauthorized
                    .body("Error: Invalid username or password!");
        } catch (AuthenticationException ex) {
            // 7. Xử lý các lỗi xác thực khác
            logger.error("Authentication failed: {}", ex.getMessage()); // Ghi log lỗi chi tiết
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Authentication failed. " + ex.getMessage());
        } catch (Exception ex) {
            // 8. Xử lý các lỗi không mong muốn khác
            logger.error("An unexpected error occurred during login: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An internal server error occurred.");
        }
    }

    // Có thể thêm các endpoint khác như /register, /refresh-token sau này
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    // --- ENDPOINT MỚI: REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            User result = authService.registerUser(registerRequest);

            // (Tùy chọn) Tạo URI cho user mới tạo (không bắt buộc)
            // URI location = ServletUriComponentsBuilder
            //         .fromCurrentContextPath().path("/api/users/{username}") // Giả sử có API lấy user theo username
            //         .buildAndExpand(result.getUsername()).toUri();

            // Trả về 201 Created với thông báo thành công
            // return ResponseEntity.created(location).body(Map.of("message", "User registered successfully!"));
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully!"));


        } catch (BadRequestException ex) { // Bắt lỗi từ AuthService (VD: username tồn tại)
            logger.warn("Registration failed for {}: {}", registerRequest.getUsername(), ex.getMessage());
            // Trả về 400 Bad Request (hoặc 409 Conflict nếu bạn dùng exception riêng)
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("An unexpected error occurred during registration: {}", ex.getMessage(), ex); // Log cả stacktrace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An internal server error occurred during registration."));
        }
    }

}