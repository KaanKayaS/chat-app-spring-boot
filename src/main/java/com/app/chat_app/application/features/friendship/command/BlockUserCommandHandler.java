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
import com.app.chat_app.persistence.repository.FriendshipRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class BlockUserCommandHandler implements CommandHandler<BlockUserCommand, FriendshipDto> {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRules friendshipRules;
    private final FriendshipMapper friendshipMapper;
    private final UserContext userContext;

    public BlockUserCommandHandler(UserRepository userRepository,
                                   FriendshipRepository friendshipRepository,
                                   FriendshipRules friendshipRules,
                                   FriendshipMapper friendshipMapper,
                                   UserContext userContext) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipRules = friendshipRules;
        this.friendshipMapper = friendshipMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public FriendshipDto handle(BlockUserCommand command) {
        UUID blockerId = UUID.fromString(userContext.getUserId());

        friendshipRules.cannotSendToSelf(blockerId, command.targetUserId());

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(UnauthenticatedException::new);

        User target = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "Kullanıcı bulunamadı."));

        Friendship friendship = friendshipRepository.findBetween(blockerId, command.targetUserId())
                .orElseGet(() -> {
                    // Aralarında kayıt yoksa yeni oluştur (doğrudan blok)
                    Friendship newFriendship = new Friendship(blocker, target);
                    return friendshipRepository.save(newFriendship);
                });

        friendship.block();

        return friendshipMapper.toDto(friendship);
    }
}
