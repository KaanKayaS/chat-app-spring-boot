package com.app.chat_app.application.features.auth.command;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.chat_app.application.features.auth.mapper.AuthMapper;
import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.core.exception.type.InvalidRefreshTokenException;
import com.app.chat_app.core.mediator.cqrs.CommandHandler;
import com.app.chat_app.core.security.jwt.JwtService;
import com.app.chat_app.core.security.jwt.JwtService.TokenPair;
import com.app.chat_app.domain.entity.User;
import com.app.chat_app.persistence.repository.UserRepository;

/**
 * Refresh akışı:
 *   1) Gelen refresh token ile DB'den user'ı bul
 *   2) Token süresi geçmemiş ve hâlâ DB'de bu kullanıcıda yazılı mı doğrula
 *   3) Yeni bir TokenPair üret → eski refresh token'ı geçersiz kıl (rotation)
 *   4) Yeni pair'i hem response'a koy hem DB'ye yaz
 *
 * Bu endpoint authenticated DEĞİL (access token expire olunca da çağrılması lazım),
 * o yüzden Command'a AuthorizableRequest implement EDİLMEDİ.
 */
@Component
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, AuthResponse> {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public RefreshTokenCommandHandler(UserRepository userRepository,
                                      JwtService jwtService,
                                      AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Override
    @Transactional
    public AuthResponse handle(RefreshTokenCommand command) {
        User user = userRepository.findByRefreshToken(command.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        // Süresi geçmiş veya başka biri tarafından değiştirilmişse:
        if (!user.isRefreshTokenValid(command.refreshToken())) {
            // Defansif: bu duruma denk gelirsek user'ın token'ını da sıfırlayalım,
            // potansiyel olarak çalınmış token'ın işe yaramamasını sağlar.
            user.clearRefreshToken();
            throw new InvalidRefreshTokenException();
        }

        // Rotation: yeni pair üret, eski refresh'i geçersizleştir.
        TokenPair pair = jwtService.generateTokenPair(user.getId(), user.getEmail());
        user.setRefreshToken(pair.refreshToken(), pair.refreshTokenExpiresAt());

        return authMapper.toAuthResponse(user, pair);
    }
}
