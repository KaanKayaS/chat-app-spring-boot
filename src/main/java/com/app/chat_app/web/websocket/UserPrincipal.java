package com.app.chat_app.web.websocket;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket session'larında Principal olarak kullanılan basit wrapper.
 * Spring STOMP, user destination routing için Principal.getName() değerini kullanır.
 * Biz buraya userId (UUID string) koyuyoruz.
 *
 * /user/queue/notifications gibi kişisel topic'lere gönderim:
 *   publisher.publishToUser(userId.toString(), payload)
 * bu Principal.getName() ile eşleşince doğru kişiye gider.
 */
public record UserPrincipal(String name) implements Principal {

    public static UserPrincipal of(UUID userId) {
        return new UserPrincipal(userId.toString());
    }

    @Override
    public String getName() {
        return name;
    }
}
