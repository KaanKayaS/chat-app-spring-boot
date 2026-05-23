package com.app.chat_app.application.features.friendship.mapper;

import java.time.Instant;
import java.util.UUID;

import com.app.chat_app.domain.entity.FriendshipStatus;

public record FriendshipDto(
        UUID id,
        UUID requesterId,
        String requesterName,
        String requesterFriendCode,
        UUID addresseeId,
        String addresseeName,
        String addresseeFriendCode,
        FriendshipStatus status,
        Instant createdAt,
        Instant respondedAt
) { }
