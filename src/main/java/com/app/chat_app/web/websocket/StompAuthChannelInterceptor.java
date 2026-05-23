package com.app.chat_app.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP CONNECT frame'ini yakalayıp JWT'den Principal oluşturur.
 *
 * Bu interceptor olmadan:
 *   - @MessageMapping metotlarında Principal her zaman null gelir
 *   - /user/queue/... kişisel topic'lere mesaj iletilemez
 *   - Disconnect event'inde kimin ayrıldığı bilinemez
 *
 * Bu interceptor ile:
 *   - accessor.setUser(UserPrincipal.of(userId)) → tüm pipeline'a Principal enjekte olur
 *   - ChatWebSocketController.markRead(request, principal) düzgün çalışır
 *   - publisher.publishToUser(userId, payload) doğru kişiye gider
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final WebSocketJwtResolver jwtResolver;

    public StompAuthChannelInterceptor(WebSocketJwtResolver jwtResolver) {
        this.jwtResolver = jwtResolver;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("STOMP CONNECT: Authorization header yok, anonymous bağlantı.");
            return message;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        jwtResolver.resolveUserId(token).ifPresentOrElse(
                userId -> {
                    accessor.setUser(UserPrincipal.of(userId));
                    log.debug("STOMP CONNECT: Principal set edildi, userId={}", userId);
                },
                () -> log.debug("STOMP CONNECT: Token geçersiz, anonymous bağlantı.")
        );

        return message;
    }
}
