package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditMessageCommand(
        @NotNull UUID messageId,
        @NotBlank @Size(max = 4000) String newContent
) implements Command<MessageDto>, AuthorizableRequest { }
