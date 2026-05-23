package com.app.chat_app.core.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.core.security.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserContext userContext;

    public JwtAuthFilter(JwtService jwtService, UserContext userContext) {
        this.jwtService = jwtService;
        this.userContext = userContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);

        // Bearer dışındaki şemaları (Basic, vb.) yok say; eksik/kısa header'da da işlem yapma.
        if (header != null && header.startsWith(BEARER_PREFIX) && header.length() > BEARER_PREFIX.length()) {
            String token = header.substring(BEARER_PREFIX.length());

            // Tek parse: validate + claim çıkarma birlikte.
            jwtService.parseAccessToken(token).ifPresent(claims -> {
                List<String> roles = List.of(); // TODO: User entity'de roller olunca buradan doldurulacak
                userContext.setUser(claims.userId().toString(), claims.email(), roles);
            });
        }

        // Token yok/geçersiz olsa bile filter chain devam etmeli.
        // Yetkilendirme kararı AuthorizationBehavior'da veriliyor (handler'a girince).
        filterChain.doFilter(request, response);
    }
}
