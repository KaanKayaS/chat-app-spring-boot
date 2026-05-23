package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Chat;
import com.app.chat_app.domain.entity.Message;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;
import com.app.chat_app.persistence.repository.ChatRepository;
import com.app.chat_app.persistence.repository.MessageRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class SendMessageCommandHandler implements CommandHandler<SendMessageCommand, MessageDto> {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final WebSocketMessagePublisher publisher;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public SendMessageCommandHandler(UserRepository userRepository,
                                     ChatRepository chatRepository,
                                     ChatParticipantRepository participantRepository,
                                     MessageRepository messageRepository,
                                     WebSocketMessagePublisher publisher,
                                     ChatMapper chatMapper,
                                     UserContext userContext) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.publisher = publisher;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public MessageDto handle(SendMessageCommand command) {
        UUID senderId = UUID.fromString(userContext.getUserId());

        User sender = userRepository.findById(senderId)
                .orElseThrow(UnauthenticatedException::new);

        Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CHAT_NOT_FOUND", "Sohbet bulunamadı."));

        // Gönderenin bu sohbetin aktif üyesi olup olmadığını kontrol et
        boolean isMember = participantRepository
                .existsByChatIdAndUserIdAndLeftAtIsNull(chat.getId(), senderId);
        if (!isMember) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "NOT_CHAT_MEMBER", "Bu sohbetin üyesi değilsin.");
        }

        Message message = Message.text(chat, sender, command.content());
        messageRepository.save(message);

        MessageDto dto = chatMapper.toMessageDto(message);

        // Transaction commit olduktan SONRA yayınla.
        // Commit öncesi yayınlarsak alıcı hemen DB'ye sorgu attığında mesajı göremeyebilir.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publisher.publishMessage(chat.getId(), dto);
            }
        });

        return dto;
    }
}
