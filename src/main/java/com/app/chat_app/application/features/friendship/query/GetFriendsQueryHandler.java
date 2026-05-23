package com.app.chat_app.application.features.friendship.query;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.friendship.mapper.FriendDto;
import com.app.chat_app.application.features.friendship.mapper.FriendshipMapper;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.persistence.repository.FriendshipRepository;

@Component
public class GetFriendsQueryHandler implements QueryHandler<GetFriendsQuery, PagedResponse<FriendDto>> {

    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper friendshipMapper;
    private final UserContext userContext;

    public GetFriendsQueryHandler(FriendshipRepository friendshipRepository,
                                   FriendshipMapper friendshipMapper,
                                   UserContext userContext) {
        this.friendshipRepository = friendshipRepository;
        this.friendshipMapper = friendshipMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FriendDto> handle(GetFriendsQuery query) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        var page = friendshipRepository.findAcceptedFriendships(
                currentUserId,
                PageRequest.of(query.page(), query.size())
        );

        var items = page.getContent()
                .stream()
                .map(f -> friendshipMapper.toFriendDto(f, currentUserId))
                .toList();

        return PagedResponse.of(items, query.page(), query.size(), page.getTotalElements());
    }
}
