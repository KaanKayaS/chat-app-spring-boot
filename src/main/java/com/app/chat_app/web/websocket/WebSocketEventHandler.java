package com.app.chat_app.web.websocket;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.app.chat_app.application.features.presence.command.SetUserOfflineCommand;
import com.app.chat_app.application.features.presence.command.SetUserOnlineCommand;
import com.app.chat_app.core.mediator.Mediator;

/**
 * .NET Hub.OnConnectedAsync / OnDisconnectedAsync karşılığı.
 *
 * NOT — Event seçimi:
 *   SessionConnectedEvent: STOMP CONNECTED frame gönderildikten SONRA (bağlantı kuruldu).
 *   Bu noktada StompAuthChannelInterceptor Principal'ı çoktan set etmiştir.
 *   accessor.getUser() ile Principal'a güvenle erişebiliriz.
 *
 *   Native header'ları (Authorization) okumak için SessionConnectEvent kullanılır.
 *   Biz artık buna gerek duymuyoruz — interceptor Principal'ı zaten koydu.
 */
@Component
public class WebSocketEventHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventHandler.class);

    private final Mediator mediator;

    public WebSocketEventHandler(Mediator mediator) {
        this.mediator = mediator;
    }

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // Principal, StompAuthChannelInterceptor tarafından CONNECT anında set edildi.
        if (accessor.getUser() == null) {
            log.debug("Anonymous WebSocket bağlantısı: session={}", sessionId);
            return;
        }

        try {
            UUID userId = UUID.fromString(accessor.getUser().getName());
            mediator.send(new SetUserOnlineCommand(userId, sessionId));
            log.debug("User online: userId={} session={}", userId, sessionId);
        } catch (IllegalArgumentException e) {
            log.warn("Principal.getName() geçerli bir UUID değil: {}", accessor.getUser().getName());
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        mediator.send(new SetUserOfflineCommand(sessionId));
        log.debug("Session disconnect: session={}", sessionId);
    }
}
