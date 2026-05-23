package com.app.chat_app.application.features.presence.command;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.presence.service.PresenceStore;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class SetUserOfflineCommandHandler implements CommandHandler<SetUserOfflineCommand, Void> {

    private final PresenceStore presenceStore;
    private final UserRepository userRepository;
    private final WebSocketMessagePublisher publisher;

    public SetUserOfflineCommandHandler(PresenceStore presenceStore,
                                        UserRepository userRepository,
                                        WebSocketMessagePublisher publisher) {
        this.presenceStore = presenceStore;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void handle(SetUserOfflineCommand command) {
        var userId = presenceStore.disconnect(command.sessionId());
        if (userId == null) return null; // zaten kayıtlı değildi

        // lastSeenAt = "en son ne zaman online'dı" — offline olma anı
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastSeenAt(Instant.now());
        });

        // Aynı kullanıcının başka bir session'ı varsa hâlâ online sayılır
        if (!presenceStore.isOnline(userId)) {
            publisher.publishPresence(new SetUserOnlineCommandHandler.PresenceEvent(userId, false));
        }

        return null;
    }
}
