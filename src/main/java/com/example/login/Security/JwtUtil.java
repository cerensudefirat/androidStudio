package com.example.login.Security;

import io.jsonwebtoken.Claims; // BU EKLENDİ
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails; // BU EKLENDİ
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function; // BU EKLENDİ

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 1. Token içinden Kullanıcı Adını Çıkarma (Filtre bunu kullanacak)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Token içinden Son Kullanma Tarihini Çıkarma
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 3. Token Geçerli mi Kontrolü (En Kritik Metot)
    // JwtUtil.java dosyasının içindeki o metodu bul ve ismini böyle yap:

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Kullanıcı adı doğru mu ve süresi dolmamış mı?
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Token Oluşturma (Senin yazdığın kısım)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}