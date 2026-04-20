package com.masterly.core.security;

import com.masterly.core.entity.Master;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитный класс для работы с JWT токенами.
 * Предоставляет методы для генерации, валидации и извлечения данных из токенов.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Извлечь имя пользователя (email) из токена.
     *
     * @param token JWT токен
     * @return email пользователя
     */
    public String extractUsername(String token) {
        log.debug("Extracting username from token");
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлечь дату истечения срока действия токена.
     *
     * @param token JWT токен
     * @return дата истечения
     */
    public Date extractExpiration(String token) {
        log.debug("Extracting expiration from token");
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечь произвольный claim из токена.
     *
     * @param token          JWT токен
     * @param claimsResolver функция для извлечения нужного claim
     * @param <T>            тип возвращаемого значения
     * @return значение claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        log.debug("Token expired: {}", expired);
        return expired;
    }

    /**
     * Сгенерировать JWT токен для пользователя.
     *
     * @param userDetails данные пользователя
     * @return JWT токен
     */
    public String generateToken(UserDetails userDetails) {
        log.debug("Generating token for user: {}", userDetails.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        if (userDetails instanceof Master) {
            Master master = (Master) userDetails;
            claims.put("name", master.getFullName());
            log.debug("Added user name to token: {}", master.getFullName());
        }

        String token = createToken(claims, userDetails.getUsername());
        log.debug("Token generated successfully for user: {}", userDetails.getUsername());

        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Проверить валидность токена.
     *
     * @param token       JWT токен
     * @param userDetails данные пользователя для сверки
     * @return true если токен валиден и не истёк
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        if (!isValid) {
            log.warn("Token validation failed for user: {}", username);
        } else {
            log.debug("Token validated successfully for user: {}", username);
        }

        return isValid;
    }
}