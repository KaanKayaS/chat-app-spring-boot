package com.app.chat_app.web.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;

/**
 * STOMP mesaj handler'ları — .NET Hub metodları gibi.
 *
 * StompAuthChannelInterceptor Principal'ı set ettiği için
 * Principal principal parametresi artık null gelmez (token geçerliyse).
 *
 * Mevcut:
 *   /app/chat.read  → okundu işaretle + broadcast
 *
 * İlerde buraya eklenebilir:
 *   /app/chat.typing → "yazıyor..." bildirimi (DB'ye yazılmaz, direkt broadcast)
 */
@Controller
public class ChatWebSocketController {

    private final ChatParticipantRepository participantRepository;
    private final WebSocketMessagePublisher publisher;

    public ChatWebSocketController(ChatParticipantRepository participantRepository,
                                   WebSocketMessagePublisher publisher) {
        this.participantRepository = participantRepository;
        this.publisher = publisher;
    }

    /**
     * Client şunu gönderir:
     *   destination: /app/chat.read
     *   body: { "chatId": "...", "lastReadMessageId": "..." }
     *
     * Sunucu:
     *   1) ChatParticipant.lastReadMessageId günceller
     *   2) /topic/chat.{chatId}.read topic'ine ReadReceiptEvent yayınlar
     */
    @MessageMapping("chat.read")
    public void markRead(@Payload MarkReadRequest request, Principal principal) {
        if (principal == null) return;

        UUID userId;
        try {
            userId = UUID.fromString(principal.getName());
        } catch (IllegalArgumentException e) {
            return;
        }

        participantRepository
                .findByChatIdAndUserId(request.chatId(), userId)
                .ifPresent(participant -> {
                    participant.markRead(request.lastReadMessageId());
                    participantRepository.save(participant);

                    publisher.publishReadReceipt(
                            request.chatId(),
                            new ReadReceiptEvent(request.chatId(), userId, request.lastReadMessageId())
                    );
                });
    }

    /**
     * "Yazıyor..." bildirimi — DB'ye yazılmaz, direkt broadcast.
     * Client: destination: /app/chat.typing, body: { "chatId": "..." }
     */
    @MessageMapping("chat.typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        if (principal == null) return;

        try {
            UUID userId = UUID.fromString(principal.getName());
            publisher.publishTyping(request.chatId(),
                    new TypingEvent(request.chatId(), userId));
        } catch (IllegalArgumentException e) {
            // geçersiz principal
        }
    }

    public record MarkReadRequest(UUID chatId, UUID lastReadMessageId) { }

    public record ReadReceiptEvent(UUID chatId, UUID userId, UUID lastReadMessageId) { }

    public record TypingRequest(UUID chatId) { }

    public record TypingEvent(UUID chatId, UUID userId) { }
}
