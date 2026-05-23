package com.app.chat_app.application.features.auth.rule;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.app.chat_app.persistence.repository.UserRepository;

/**
 * Kullanıcı kayıt olurken çağrılır. Diğer kullanıcılar bu kodla arkadaş ekleyebilir.
 *
 * 8 karakter, [A-Z0-9] alfabesi → 36^8 ≈ 2.8 trilyon olasılık.
 * Yine de DB'de çakışma olabilir (cosmic ray, vs.) — bu yüzden retry loop var.
 */
@Component
public class FriendCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    public FriendCodeGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUnique() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = randomCode();
            if (!userRepository.existsByFriendCode(candidate)) {
                return candidate;
            }
        }
        // 10 denemede çakışıyorsa ya RANDOM bozuk ya da DB'de çok kullanıcı var → uzunluğu artırma sinyali.
        throw new IllegalStateException("Benzersiz friend code üretilemedi (10 deneme tükendi).");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
