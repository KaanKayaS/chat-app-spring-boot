package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotNull;

/** İki kullanıcı arasında private chat aç (zaten varsa var olanı döner). */
public record CreatePrivateChatCommand(
        @NotNull UUID targetUserId
) implements Command<ChatDto>, AuthorizableRequest { }
