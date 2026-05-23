package com.app.chat_app.application.features.auth.command;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class LogoutCommandHandler implements CommandHandler<LogoutCommand, Void> {

    private final UserRepository userRepository;
    private final UserContext userContext;

    public LogoutCommandHandler(UserRepository userRepository, UserContext userContext) {
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public Void handle(LogoutCommand command) {
        // AuthorizationBehavior bizden önce çalıştığı için kullanıcı zaten authenticated.
        // Yine de defansif okuyalım.
        String userIdStr = userContext.getUserId();
        if (userIdStr == null) {
            throw new UnauthenticatedException();
        }

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(UnauthenticatedException::new);

        user.clearRefreshToken();
        return null;
    }
}
