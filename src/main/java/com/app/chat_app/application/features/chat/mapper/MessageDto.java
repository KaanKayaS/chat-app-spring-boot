package com.app.chat_app.application.features.chat.mapper;

import java.time.Instant;
import java.util.UUID;

import com.app.chat_app.domain.entity.MessageType;

public record MessageDto(
        UUID id,
        UUID chatId,
        UUID senderId,
        String senderName,
        MessageType type,
        String content,
        Instant sentAt,
        Instant editedAt,
        boolean deleted
) { }
