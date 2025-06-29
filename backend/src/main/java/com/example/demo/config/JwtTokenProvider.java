package com.example.demo.config;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecretString; // Lấy khóa bí mật từ application.properties

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationInMs; // Lấy thời gian hết hạn từ application.properties

    private SecretKey jwtSecretKey; // Khóa bí mật đã được giải mã và tạo đối tượng Key

    // Phương thức này chạy sau khi các dependency được inject
    @PostConstruct
    protected void init() {
        // Cần giải mã Base64 key từ properties nếu nó được encode, hoặc tạo Key trực tiếp
        // Cách 1: Nếu key trong properties là Base64 encoded của một key đủ dài
        // byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
        // this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);

        // Cách 2: Nếu key trong properties là string đủ dài, dùng nó để tạo key (phổ biến hơn)
        // Đảm bảo key đủ dài cho thuật toán HS512 (ít nhất 512 bits / 64 bytes)
        if (jwtSecretString.length() < 64) {
            logger.warn("JWT secret key length is less than 64 bytes, which is not recommended for HS512.");
            // Có thể ném exception hoặc dùng thuật toán yếu hơn nếu muốn, nhưng không khuyến khích
        }
        // Chuyển string thành byte array và tạo SecretKey
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());

        // Hoặc nếu muốn an toàn hơn nữa, tạo key ngẫu nhiên khi khởi động
        // this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        // Nhưng cách này sẽ làm mất hiệu lực token cũ mỗi khi restart server
    }

    // Hàm tạo JWT token từ thông tin Authentication
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal(); // Lấy user đang đăng nhập
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Subject là username
                .setIssuedAt(now) // Thời điểm phát hành
                .setExpiration(expiryDate) // Thời điểm hết hạn
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512) // Ký với khóa bí mật và thuật toán HS512
                .compact(); // Build thành chuỗi token
    }

    // Hàm tạo JWT token từ username (dùng khi không có đối tượng Authentication)
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }


    // Hàm lấy username từ JWT token
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey) // Đặt khóa để xác thực chữ ký
                .build()
                .parseClaimsJws(token) // Giải mã và xác thực token
                .getBody(); // Lấy phần payload (claims)

        return claims.getSubject(); // Lấy username từ subject
    }

    // Hàm xác thực JWT token
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true; // Token hợp lệ (chữ ký đúng, chưa hết hạn)
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false; // Token không hợp lệ
    }
}