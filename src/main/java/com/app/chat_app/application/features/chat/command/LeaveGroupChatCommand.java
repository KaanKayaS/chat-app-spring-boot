package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotNull;

public record LeaveGroupChatCommand(
        @NotNull UUID chatId
) implements Command<Void>, AuthorizableRequest { }
