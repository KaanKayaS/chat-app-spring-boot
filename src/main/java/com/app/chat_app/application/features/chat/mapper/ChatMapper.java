package com.app.chat_app.application.features.chat.mapper;

import org.springframework.stereotype.Component;

import com.app.chat_app.domain.entity.Chat;
import com.app.chat_app.domain.entity.Message;

@Component
public class ChatMapper {

    public ChatDto toChatDto(Chat chat) {
        return new ChatDto(
                chat.getId(),
                chat.getType(),
                chat.getName(),
                chat.getAvatarUrl(),
                chat.getCreatedAt()
        );
    }

    public MessageDto toMessageDto(Message message) {
        String senderName = message.getSender() != null
                ? message.getSender().fullName()
                : "System";

        // Silinmiş mesajın içeriğini sızdırma
        String content = message.isDeleted() ? null : message.getContent();

        return new MessageDto(
                message.getId(),
                message.getChat().getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                senderName,
                message.getType(),
                content,
                message.getSentAt(),
                message.getEditedAt(),
                message.isDeleted()
        );
    }
}
