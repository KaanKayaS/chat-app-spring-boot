package com.app.chat_app.application.features.chat.command;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.application.features.friendship.rule.FriendshipRules;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Chat;
import com.app.chat_app.domain.entity.ChatParticipant;
import com.app.chat_app.domain.entity.FriendshipStatus;
import com.app.chat_app.domain.entity.ParticipantRole;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;
import com.app.chat_app.persistence.repository.ChatRepository;
import com.app.chat_app.persistence.repository.FriendshipRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class CreatePrivateChatCommandHandler implements CommandHandler<CreatePrivateChatCommand, ChatDto> {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipRules friendshipRules;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public CreatePrivateChatCommandHandler(UserRepository userRepository,
                                           ChatRepository chatRepository,
                                           ChatParticipantRepository participantRepository,
                                           FriendshipRepository friendshipRepository,
                                           FriendshipRules friendshipRules,
                                           ChatMapper chatMapper,
                                           UserContext userContext) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.friendshipRepository = friendshipRepository;
        this.friendshipRules = friendshipRules;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public ChatDto handle(CreatePrivateChatCommand command) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        if (currentUserId.equals(command.targetUserId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "SELF_CHAT", "Kendinle sohbet açamazsın.");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(UnauthenticatedException::new);

        User targetUser = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "Kullanıcı bulunamadı."));

        // Arkadaş olup olmadığını kontrol et
        var friendship = friendshipRepository.findBetween(currentUserId, command.targetUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN,
                        "NOT_FRIENDS", "Sadece arkadaşlarınla özel sohbet açabilirsin."));

        friendshipRules.mustBeAccepted(friendship.getStatus());

        // "find or create" — zaten varsa var olanı döner
        return chatRepository.findPrivateChatBetween(currentUserId, command.targetUserId())
                .map(chatMapper::toChatDto)
                .orElseGet(() -> {
                    Chat chat = Chat.privateChat();
                    chatRepository.save(chat);

                    participantRepository.save(new ChatParticipant(chat, currentUser, ParticipantRole.MEMBER));
                    participantRepository.save(new ChatParticipant(chat, targetUser, ParticipantRole.MEMBER));

                    return chatMapper.toChatDto(chat);
                });
    }
}
