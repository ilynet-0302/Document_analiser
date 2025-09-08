package com.example.Document_analiser.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

/**
 * Помощен клас за издаване и валидиране на JWT токени.
 * - SECRET_KEY и EXPIRATION са примерни и следва да се конфигурират безопасно.
 */
@Component
public class JwtUtil {
    private final String SECRET_KEY = "secret-key";
    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours

    /** Генерира подписан JWT за дадено потребителско име. */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /** Извлича потребителското име (subject) от токена. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Връща момента на изтичане на токена. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Общ метод за извличане на claim от токена чрез предадена функция.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    /** Проверява, че токенът е за същия потребител и не е изтекъл. */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
} 
