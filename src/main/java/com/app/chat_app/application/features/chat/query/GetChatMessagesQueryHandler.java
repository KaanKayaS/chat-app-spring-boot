package com.app.chat_app.application.features.chat.query;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.persistence.repository.ChatParticipantRepository;
import com.app.chat_app.persistence.repository.MessageRepository;

@Component
public class GetChatMessagesQueryHandler
        implements QueryHandler<GetChatMessagesQuery, PagedResponse<MessageDto>> {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public GetChatMessagesQueryHandler(MessageRepository messageRepository,
                                       ChatParticipantRepository participantRepository,
                                       ChatMapper chatMapper,
                                       UserContext userContext) {
        this.messageRepository = messageRepository;
        this.participantRepository = participantRepository;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MessageDto> handle(GetChatMessagesQuery query) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        boolean isMember = participantRepository
                .existsByChatIdAndUserIdAndLeftAtIsNull(query.chatId(), currentUserId);
        if (!isMember) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "NOT_CHAT_MEMBER", "Bu sohbetin üyesi değilsin.");
        }

        var page = messageRepository.findByChatIdOrderBySentAtDesc(
                query.chatId(),
                PageRequest.of(query.page(), query.size())
        );

        return PagedResponse.from(page.map(chatMapper::toMessageDto));
    }
}
