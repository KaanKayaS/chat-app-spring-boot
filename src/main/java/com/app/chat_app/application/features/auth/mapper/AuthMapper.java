package com.app.chat_app.application.features.auth.mapper;

import org.springframework.stereotype.Component;

import com.app.chat_app.core.security.jwt.JwtService.TokenPair;
import com.app.chat_app.domain.entity.User;

@Component
public class AuthMapper {

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFriendCode(),
                user.isEmailConfirmed()
        );
    }

    public AuthResponse toAuthResponse(User user, TokenPair pair) {
        return new AuthResponse(
                toUserDto(user),
                pair.accessToken(),
                pair.refreshToken(),
                pair.refreshTokenExpiresAt()
        );
    }
}
