package com.app.chat_app.application.features.friendship.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.application.features.friendship.mapper.FriendshipMapper;
import com.app.chat_app.application.features.friendship.rule.FriendshipRules;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Friendship;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.FriendshipRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class SendFriendRequestCommandHandler implements CommandHandler<SendFriendRequestCommand, FriendshipDto> {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRules friendshipRules;
    private final FriendshipMapper friendshipMapper;
    private final WebSocketMessagePublisher publisher;
    private final UserContext userContext;

    public SendFriendRequestCommandHandler(UserRepository userRepository,
                                           FriendshipRepository friendshipRepository,
                                           FriendshipRules friendshipRules,
                                           FriendshipMapper friendshipMapper,
                                           WebSocketMessagePublisher publisher,
                                           UserContext userContext) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipRules = friendshipRules;
        this.friendshipMapper = friendshipMapper;
        this.publisher = publisher;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public FriendshipDto handle(SendFriendRequestCommand command) {
        UUID requesterId = UUID.fromString(userContext.getUserId());

        User requester = userRepository.findById(requesterId)
                .orElseThrow(UnauthenticatedException::new);

        User addressee = userRepository.findByFriendCode(command.friendCode().toUpperCase())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "Bu friend code'a sahip kullanıcı bulunamadı."));

        friendshipRules.cannotSendToSelf(requesterId, addressee.getId());
        friendshipRules.mustNotHaveActiveFriendship(requesterId, addressee.getId());

        Friendship friendship = new Friendship(requester, addressee);
        friendshipRepository.save(friendship);

        FriendshipDto dto = friendshipMapper.toDto(friendship);

        // Addressee online ise anlık bildirim
        publisher.publishToUser(addressee.getId().toString(), dto);

        return dto;
    }
}
