package com.app.chat_app.application.features.chat.command;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.Chat;
import com.app.chat_app.domain.entity.ChatParticipant;
import com.app.chat_app.domain.entity.Message;
import com.app.chat_app.domain.entity.ParticipantRole;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;
import com.app.chat_app.persistence.repository.ChatRepository;
import com.app.chat_app.persistence.repository.MessageRepository;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class CreateGroupChatCommandHandler implements CommandHandler<CreateGroupChatCommand, ChatDto> {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public CreateGroupChatCommandHandler(UserRepository userRepository,
                                         ChatRepository chatRepository,
                                         ChatParticipantRepository participantRepository,
                                         MessageRepository messageRepository,
                                         ChatMapper chatMapper,
                                         UserContext userContext) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional
    public ChatDto handle(CreateGroupChatCommand command) {
        UUID creatorId = UUID.fromString(userContext.getUserId());

        User creator = userRepository.findById(creatorId)
                .orElseThrow(UnauthenticatedException::new);

        // memberIds içinde kurucu varsa çıkar (zaten OWNER olarak ekleyeceğiz)
        List<UUID> otherMemberIds = command.memberIds().stream()
                .filter(id -> !id.equals(creatorId))
                .distinct()
                .toList();

        List<User> members = userRepository.findAllById(otherMemberIds);

        if (members.size() != otherMemberIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "MEMBER_NOT_FOUND", "Bazı kullanıcılar bulunamadı.");
        }

        Chat chat = Chat.group(command.name(), creator);
        chatRepository.save(chat);

        // Kurucuyu OWNER olarak ekle
        participantRepository.save(new ChatParticipant(chat, creator, ParticipantRole.OWNER));

        // Diğer üyeleri MEMBER olarak ekle
        members.forEach(member ->
                participantRepository.save(new ChatParticipant(chat, member, ParticipantRole.MEMBER))
        );

        messageRepository.save(Message.system(chat,
                creator.fullName() + " grubu oluşturdu."));

        return chatMapper.toChatDto(chat);
    }
}
