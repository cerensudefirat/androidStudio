package com.example.login.config;

import com.example.login.Security.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    private String mask(String s) {
        if (s == null) return null;
        if (s.length() <= 12) return "*****";
        return s.substring(0, 6) + "..." + s.substring(s.length() - 6);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7).trim();

        if (jwt.isEmpty()) {
            logger.warn("Authorization header var fakat token boş. Header(masked): {}", mask(authHeader));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization token");
            return;
        }

        long dotCount = jwt.chars().filter(ch -> ch == '.').count();
        if (dotCount != 2) {
            // TOKEN İÇİNDE YASAK KARAKTER VAR/YA DA PARÇALAR EKSİK
            logger.warn("Geçersiz JWT formatı (parça sayısı != 3). Header(masked): {}", mask(authHeader));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization token format");
            return;
        }

        // Eğer token içinde ':' veya boşluk gibi kesinlikle olmaması gereken karakterler varsa reddet
        if (jwt.indexOf(':') >= 0 || jwt.indexOf(' ') >= 0) {
            logger.warn("JWT içinde yasaklı karakter bulundu. Header(masked): {}", mask(authHeader));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization token characters");
            return;
        }

        String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("JWT parse/decoding hatası. Header(masked): {} - hata: {}", mask(authHeader), ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        } catch (Exception ex) {
            logger.warn("Beklenmeyen JWT işleme hatası. Header(masked): {} - hata: {}", mask(authHeader), ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                logger.warn("JWT içindeki kullanıcı bulunamadı: {} Header(masked): {}", username, mask(authHeader));
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            } catch (Exception ex) {
                logger.warn("UserDetailsService hatası: {} Header(masked): {}", ex.getMessage(), mask(authHeader));
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
                return;
            }

            try {
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.debug("JWT geçersiz veya süresi dolmuş. Kullanıcı: {} Header(masked): {}", username, mask(authHeader));
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT expired or invalid");
                    return;
                }
            } catch (Exception ex) {
                logger.warn("Token validasyonunda hata: {} Header(masked): {}", ex.getMessage(), mask(authHeader));
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    // debug helper - JwtAuthenticationFilter içinde kullan
    private void logAllAuthorizationHeaders(HttpServletRequest request) {
        Enumeration<String> en = request.getHeaders("Authorization");
        int i = 0;
        while (en.hasMoreElements()) {
            String h = en.nextElement();
            String masked = (h == null) ? "null" : (h.length() <= 16 ? "*****" : h.substring(0,6) + "..." + h.substring(h.length()-6));
            logger.warn("Authorization header #{} (masked): {}", ++i, masked);
        }
    }

}
