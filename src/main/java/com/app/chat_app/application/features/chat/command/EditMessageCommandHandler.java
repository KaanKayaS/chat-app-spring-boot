package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Message;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.MessageRepository;

@Component
public class EditMessageCommandHandler implements CommandHandler<EditMessageCommand, MessageDto> {

    private final MessageRepository messageRepository;
    private final WebSocketMessagePublisher publisher;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public EditMessageCommandHandler(MessageRepository messageRepository,
                                     WebSocketMessagePublisher publisher,
                                     ChatMapper chatMapper,
                                     UserContext userContext) {
        this.messageRepository = messageRepository;
        this.publisher = publisher;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public MessageDto handle(EditMessageCommand command) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        Message message = messageRepository.findById(command.messageId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "MESSAGE_NOT_FOUND", "Mesaj bulunamadı."));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "NOT_MESSAGE_OWNER", "Sadece kendi mesajını düzenleyebilirsin.");
        }

        message.edit(command.newContent());

        MessageDto dto = chatMapper.toMessageDto(message);
        // Düzenlemeyi sohbetteki herkese bildir
        publisher.publishMessage(message.getChat().getId(), dto);

        return dto;
    }
}
