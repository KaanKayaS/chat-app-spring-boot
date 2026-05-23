package com.app.chat_app.application.features.friendship.query;

import com.app.chat_app.application.features.friendship.mapper.FriendDto;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.cqrs.Query;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GetFriendsQuery(
        @Min(0) int page,
        @Min(1) @Max(100) int size
) implements Query<PagedResponse<FriendDto>>, AuthorizableRequest { }
