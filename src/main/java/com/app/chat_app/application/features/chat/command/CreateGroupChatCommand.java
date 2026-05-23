package com.app.chat_app.application.features.chat.command;

import java.util.List;
import java.util.UUID;

import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupChatCommand(
        @NotBlank @Size(min = 1, max = 128) String name,
        @NotNull @Size(min = 1, max = 99) List<UUID> memberIds 
) implements Command<ChatDto>, AuthorizableRequest { }
