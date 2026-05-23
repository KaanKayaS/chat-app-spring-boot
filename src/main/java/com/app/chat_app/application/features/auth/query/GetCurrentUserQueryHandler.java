package com.app.chat_app.application.features.auth.query;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.auth.mapper.AuthMapper;
import com.app.chat_app.application.features.auth.mapper.UserDto;
import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.cqrs.QueryHandler;
import com.app.chat_app.core.security.context.UserContext;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class GetCurrentUserQueryHandler implements QueryHandler<GetCurrentUserQuery, UserDto> {

    private final UserRepository userRepository;
    private final UserContext userContext;
    private final AuthMapper authMapper;

    public GetCurrentUserQueryHandler(UserRepository userRepository,
                                      UserContext userContext,
                                      AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.userContext = userContext;
        this.authMapper = authMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto handle(GetCurrentUserQuery query) {
        UUID userId = UUID.fromString(userContext.getUserId());

        // Token'da olup DB'de olmayan user → güvenlik anomalisi, 401 dön.
        User user = userRepository.findById(userId)
                .orElseThrow(UnauthenticatedException::new);

        return authMapper.toUserDto(user);
    }
}
