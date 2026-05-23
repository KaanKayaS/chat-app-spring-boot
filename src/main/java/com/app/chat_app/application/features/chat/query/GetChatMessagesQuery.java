package com.app.chat_app.application.features.chat.query;

import java.util.UUID;

import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.cqrs.Query;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Sohbetin mesaj geçmişini sayfalı döner (yeniden eskiye — infinite scroll için).
 * page=0, size=50 → en son 50 mesaj
 */
public record GetChatMessagesQuery(
        @NotNull UUID chatId,
        @Min(0) int page,
        @Min(1) @Max(100) int size
) implements Query<PagedResponse<MessageDto>>, AuthorizableRequest { }
