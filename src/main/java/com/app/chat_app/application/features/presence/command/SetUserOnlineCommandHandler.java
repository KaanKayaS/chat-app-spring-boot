package com.app.chat_app.application.features.presence.command;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.presence.service.PresenceStore;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class SetUserOnlineCommandHandler implements CommandHandler<SetUserOnlineCommand, Void> {

    private final PresenceStore presenceStore;
    private final UserRepository userRepository;
    private final WebSocketMessagePublisher publisher;

    public SetUserOnlineCommandHandler(PresenceStore presenceStore,
                                       UserRepository userRepository,
                                       WebSocketMessagePublisher publisher) {
        this.presenceStore = presenceStore;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void handle(SetUserOnlineCommand command) {
        presenceStore.connect(command.sessionId(), command.userId());

        // lastSeenAt güncelle (online olma zamanı)
        userRepository.findById(command.userId()).ifPresent(user -> {
            user.setLastSeenAt(Instant.now());
        });

        publisher.publishPresence(new PresenceEvent(command.userId(), true));
        return null;
    }

    public record PresenceEvent(java.util.UUID userId, boolean online) { }
}
