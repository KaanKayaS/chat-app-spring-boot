package com.app.chat_app.application.features.friendship.mapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Arkadaş listesi için hafif görünüm — diğer kullanıcının profili + online bilgisi.
 */
public record FriendDto(
        UUID userId,
        String firstName,
        String lastName,
        String friendCode,
        boolean online,
        Instant lastSeenAt
) { }
