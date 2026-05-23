package com.app.chat_app.application.features.auth.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.auth.mapper.AuthMapper;
import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.core.exception.type.InvalidCredentialsException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.jwt.JwtService;
import com.app.chat_app.core.security.jwt.JwtService.TokenPair;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class LoginCommandHandler implements CommandHandler<LoginCommand, AuthResponse> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public LoginCommandHandler(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Override
    @Transactional
    public AuthResponse handle(LoginCommand command) {
        String email = command.email().trim().toLowerCase();

        // User enumeration koruması: email yoksa da, şifre yanlışsa da AYNI exception.
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenPair pair = jwtService.generateTokenPair(user.getId(), user.getEmail());
        user.setRefreshToken(pair.refreshToken(), pair.refreshTokenExpiresAt());

        return authMapper.toAuthResponse(user, pair);
    }
}
