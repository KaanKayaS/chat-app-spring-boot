package com.app.chat_app.application.features.friendship.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.friendship.command.RespondFriendRequestCommand.FriendRequestResponse;
import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.application.features.friendship.mapper.FriendshipMapper;
import com.app.chat_app.application.features.friendship.rule.FriendshipRules;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Friendship;
import com.app.chat_app.infrastructure.websocket.WebSocketMessagePublisher;
import com.app.chat_app.persistence.repository.FriendshipRepository;

@Component
public class RespondFriendRequestCommandHandler
        implements CommandHandler<RespondFriendRequestCommand, FriendshipDto> {

    private final FriendshipRepository friendshipRepository;
    private final FriendshipRules friendshipRules;
    private final FriendshipMapper friendshipMapper;
    private final WebSocketMessagePublisher publisher;
    private final UserContext userContext;

    public RespondFriendRequestCommandHandler(FriendshipRepository friendshipRepository,
                                              FriendshipRules friendshipRules,
                                              FriendshipMapper friendshipMapper,
                                              WebSocketMessagePublisher publisher,
                                              UserContext userContext) {
        this.friendshipRepository = friendshipRepository;
        this.friendshipRules = friendshipRules;
        this.friendshipMapper = friendshipMapper;
        this.publisher = publisher;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public FriendshipDto handle(RespondFriendRequestCommand command) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        Friendship friendship = friendshipRepository.findById(command.friendshipId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "FRIENDSHIP_NOT_FOUND", "Arkadaşlık isteği bulunamadı."));

        // Sadece isteği alan kişi kabul/reddedebilir
        friendshipRules.mustBeAddresseeToRespond(currentUserId, friendship.getAddressee().getId());

        if (command.response() == FriendRequestResponse.ACCEPT) {
            friendship.accept();
        } else {
            friendship.reject();
        }

        FriendshipDto dto = friendshipMapper.toDto(friendship);

        // İsteği gönderen kişiye anlık bildirim
        publisher.publishToUser(friendship.getRequester().getId().toString(), dto);

        return dto;
    }
}
