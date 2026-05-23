package com.app.chat_app.application.features.friendship.command;

import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Friend code ile arkadaşlık isteği gönder. */
public record SendFriendRequestCommand(
        @NotBlank @Size(min = 8, max = 8) String friendCode
) implements Command<FriendshipDto>, AuthorizableRequest { }
