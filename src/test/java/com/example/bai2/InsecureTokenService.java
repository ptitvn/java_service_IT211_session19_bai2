package com.example.bai2;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class InsecureTokenService {
    // KHẮC PHỤC: Lấy secret key từ biến môi trường thay vì hardcode
    private static final String ENV_SECRET_KEY = System.getenv("JWT_SECRET_KEY");
    // KHẮC PHỤC: Thời gian hết hạn Access Token chỉ 15 phút
    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;

    private Key getSigningKey() {
        if (ENV_SECRET_KEY == null || ENV_SECRET_KEY.length() < 32) {
            System.out.println("JWT_SECRET_KEY không tồn tại hoặc quá ngắn, dùng key ngẫu nhiên để test.");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256); // fallback cho test
        }
        return Keys.hmacShaKeyFor(ENV_SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        InsecureTokenService service = new InsecureTokenService();
        String username = "testUser";

        // Tạo token hợp lệ
        String validToken = service.generateAccessToken(username);
        System.out.println("Valid Token: " + validToken);
        System.out.println("Valid Token status: " + service.validateToken(validToken));

        // Kẻ tấn công thử tạo token giả mạo với secret key sai
        String fakeSecret = "FakeSecretKeyThatIsWrongButLongEnough1234567890";
        Key attackerKey = Keys.hmacShaKeyFor(fakeSecret.getBytes());
        String forgedToken = Jwts.builder()
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(attackerKey, SignatureAlgorithm.HS256)
                .compact();
        System.out.println("\n--- Attacker attempts ---");
        System.out.println("Forged Token: " + forgedToken);
        System.out.println("Validation of forged token: " + service.validateToken(forgedToken));

        // Giả lập token hết hạn sau 15 phút
        System.out.println("\n--- Token expiration test ---");
        Thread.sleep(TimeUnit.MINUTES.toMillis(16)); // chờ 16 phút
        System.out.println("Token valid after 16 minutes: " + service.validateToken(validToken));
    }
}
