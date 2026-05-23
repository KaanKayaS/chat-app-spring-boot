package com.app.chat_app.application.features.auth.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.auth.mapper.AuthMapper;
import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.application.features.auth.rule.AuthRules;
import com.app.chat_app.application.features.auth.rule.FriendCodeGenerator;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.jwt.JwtService;
import com.app.chat_app.core.security.jwt.JwtService.TokenPair;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.UserRepository;

@Component
public class RegisterUserCommandHandler implements CommandHandler<RegisterUserCommand, AuthResponse> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendCodeGenerator friendCodeGenerator;
    private final AuthRules authRules;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public RegisterUserCommandHandler(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      FriendCodeGenerator friendCodeGenerator,
                                      AuthRules authRules,
                                      JwtService jwtService,
                                      AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.friendCodeGenerator = friendCodeGenerator;
        this.authRules = authRules;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Override
    @Transactional
    public AuthResponse handle(RegisterUserCommand command) {
        String email = command.email().trim().toLowerCase();

        authRules.emailMustBeUnique(email);

        String passwordHash = passwordEncoder.encode(command.password());
        String friendCode = friendCodeGenerator.generateUnique();

        User user = new User(email, command.firstName().trim(), command.lastName().trim(), passwordHash, friendCode);

        // Kayıttan sonra otomatik login: hemen token üretip dönüyoruz.
        TokenPair pair = jwtService.generateTokenPair(user.getId(), user.getEmail());
        user.setRefreshToken(pair.refreshToken(), pair.refreshTokenExpiresAt());

        userRepository.save(user);

        return authMapper.toAuthResponse(user, pair);
    }
}
