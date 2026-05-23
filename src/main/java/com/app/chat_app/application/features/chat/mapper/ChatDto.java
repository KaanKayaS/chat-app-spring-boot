package com.app.chat_app.application.features.chat.mapper;

import java.time.Instant;
import java.util.UUID;

import com.app.chat_app.domain.entity.ChatType;

public record ChatDto(
        UUID id,
        ChatType type,
        String name,
        String avatarUrl,
        Instant createdAt
) { }
