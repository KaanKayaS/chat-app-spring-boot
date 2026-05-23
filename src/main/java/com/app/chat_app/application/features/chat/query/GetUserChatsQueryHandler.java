package com.app.chat_app.application.features.chat.query;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.application.features.chat.mapper.ChatMapper;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.persistence.repository.ChatRepository;

@Component
public class GetUserChatsQueryHandler implements QueryHandler<GetUserChatsQuery, PagedResponse<ChatDto>> {

    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserContext userContext;

    public GetUserChatsQueryHandler(ChatRepository chatRepository,
                                    ChatMapper chatMapper,
                                    UserContext userContext) {
        this.chatRepository = chatRepository;
        this.chatMapper = chatMapper;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ChatDto> handle(GetUserChatsQuery query) {
        UUID currentUserId = UUID.fromString(userContext.getUserId());

        var page = chatRepository.findAllByUserId(
                currentUserId,
                PageRequest.of(query.page(), query.size())
        );

        return PagedResponse.from(page.map(chatMapper::toChatDto));
    }
}
