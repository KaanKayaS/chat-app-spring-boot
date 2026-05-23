package com.app.chat_app.web.websocket;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.app.chat_app.core.security.jwt.JwtService;

/**
 * WebSocket bağlantısında JWT'yi çözmek için yardımcı.
 * JwtAuthFilter HTTP request'e özgüydü; WebSocket için ayrı resolver gerekiyor.
 */
@Component
public class WebSocketJwtResolver {

    private final JwtService jwtService;

    public WebSocketJwtResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Optional<UUID> resolveUserId(String token) {
        return jwtService.parseAccessToken(token)
                .map(claims -> claims.userId());
    }
}
