package com.app.chat_app.application.features.friendship.query;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.application.features.friendship.mapper.FriendshipMapper;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.FriendshipStatus;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.FriendshipRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class GetFriendRequestsQueryHandler
        implements QueryHandler<GetFriendRequestsQuery, PagedResponse<FriendshipDto>> {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipMapper friendshipMapper;
    private final UserContext userContext;

    public GetFriendRequestsQueryHandler(FriendshipRepository friendshipRepository,
                                          UserRepository userRepository,
                                          FriendshipMapper friendshipMapper,
                                          UserContext userContext) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.friendshipMapper = friendshipMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FriendshipDto> handle(GetFriendRequestsQuery query) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(UnauthenticatedException::new);

        var page = friendshipRepository.findByAddresseeAndStatus(
                currentUser,
                FriendshipStatus.PENDING,
                PageRequest.of(query.page(), query.size())
        );

        var items = page.getContent()
                .stream()
                .map(friendshipMapper::toDto)
                .toList();

        return PagedResponse.from(page.map(f -> friendshipMapper.toDto(f)));
    }
}
