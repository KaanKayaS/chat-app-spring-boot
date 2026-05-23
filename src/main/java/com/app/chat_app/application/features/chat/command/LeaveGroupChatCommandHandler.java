package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Chat;
import com.app.chat_app.domain.entity.ChatType;
import com.app.chat_app.domain.entity.ParticipantRole;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;
import com.app.chat_app.persistence.repository.ChatRepository;

@Component
public class LeaveGroupChatCommandHandler implements CommandHandler<LeaveGroupChatCommand, Void> {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final UserContext userContext;

    public LeaveGroupChatCommandHandler(ChatRepository chatRepository,
                                        ChatParticipantRepository participantRepository,
                                        UserContext userContext) {
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public Void handle(LeaveGroupChatCommand command) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CHAT_NOT_FOUND", "Sohbet bulunamadı."));

        if (chat.getType() != ChatType.GROUP) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "NOT_GROUP_CHAT",
                    "Sadece grup sohbetlerden ayrılabilirsiniz.");
        }

        var participant = participantRepository
                .findByChatIdAndUserId(command.chatId(), currentUserId)
                .orElseThrow(() -> new UnauthenticatedException());

        if (!participant.isActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ALREADY_LEFT",
                    "Bu sohbetten zaten ayrıldınız.");
        }

        if (participant.getRole() == ParticipantRole.OWNER) {
            long remaining = participantRepository.countByChatIdAndLeftAtIsNull(command.chatId());
            if (remaining > 1) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "OWNER_MUST_TRANSFER",
                        "Gruptan ayrılmadan önce sahipliği başka bir üyeye devredin.");
            }
        }

        participant.leave();
        return null;
    }
}
