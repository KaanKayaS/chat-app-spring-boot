package com.app.chat_app.infrastructure.websocket;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * SimpMessagingTemplate'i doğrudan application/domain katmanında kullanmak yerine
 * bu wrapper üzerinden çağırıyoruz — application katmanı Spring Messaging'e
 * direkt bağımlı olmuyor.
 *
 * .NET'teki IHubContext<ChatHub> inject etmeye karşılık geliyor.
 */
@Component
public class WebSocketMessagePublisher {

    private final SimpMessagingTemplate messaging;

    public WebSocketMessagePublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** Bir sohbetin tüm abonelerine yeni mesaj gönder. */
    public void publishMessage(UUID chatId, Object payload) {
        messaging.convertAndSend("/topic/chat." + chatId, payload);
    }

    /** Bir sohbetin tüm abonelerine okundu bildirimi gönder. */
    public void publishReadReceipt(UUID chatId, Object payload) {
        messaging.convertAndSend("/topic/chat." + chatId + ".read", payload);
    }

    /** Tüm bağlı istemcilere online/offline değişimi yayınla. */
    public void publishPresence(Object payload) {
        messaging.convertAndSend("/topic/presence", payload);
    }

    /** "Yazıyor..." bildirimi — DB'ye yazılmaz, direkt broadcast. */
    public void publishTyping(UUID chatId, Object payload) {
        messaging.convertAndSend("/topic/chat." + chatId + ".typing", payload);
    }

    /** Belirli bir kullanıcıya kişisel bildirim gönder (arkadaşlık isteği vb.). */
    public void publishToUser(String userId, Object payload) {
        messaging.convertAndSendToUser(userId, "/queue/notifications", payload);
    }
}
