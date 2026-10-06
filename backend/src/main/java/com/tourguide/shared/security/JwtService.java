package com.tourguide.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Issues and verifies the same HS256 JWT shape the Node `jsonwebtoken`
 * library produced: claims sub (user_id as a string), role, name.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final Duration expiry;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expires-in:7d}") String expiresIn) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiry = parseDuration(expiresIn);
    }

    public String sign(int userId, String role, String name) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiry.toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("name", name)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Returns the verified claims, or null if the token is missing/invalid/expired. */
    public Claims verify(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public CurrentUserDetails toUser(Claims claims) {
        return new CurrentUserDetails(
                Integer.valueOf(claims.getSubject()),
                (String) claims.get("role"),
                (String) claims.get("name"));
    }

    /** Parses "7d" / "24h" / "30m" / "45s" / a bare number of seconds, like the zeit/ms strings jsonwebtoken accepts. */
    static Duration parseDuration(String value) {
        if (value == null || value.isBlank()) return Duration.ofDays(7);
        String v = value.trim();
        Pattern p = Pattern.compile("^(\\d+)\\s*([smhd])?$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(v);
        if (!m.matches()) return Duration.ofDays(7);
        long amount = Long.parseLong(m.group(1));
        String unit = m.group(2) == null ? "s" : m.group(2).toLowerCase();
        return switch (unit) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> Duration.ofDays(7);
        };
    }
}
