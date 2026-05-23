package com.app.chat_app.application.features.friendship.command;

import java.util.UUID;

import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotNull;

public record BlockUserCommand(
        @NotNull UUID targetUserId
) implements Command<FriendshipDto>, AuthorizableRequest { }
