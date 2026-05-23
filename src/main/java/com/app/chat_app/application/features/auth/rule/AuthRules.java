package com.app.chat_app.application.features.auth.rule;

import org.springframework.stereotype.Component;

import com.app.chat_app.core.exception.type.UserAlreadyExistsException;
import com.app.chat_app.persistence.repository.UserRepository;

/**
 * Auth feature'ında handler'ların çağırdığı domain kuralları.
 * Handler'ı dağınık tutmayacak şekilde, ortak kontrolleri bu sınıfta topluyoruz.
 */
@Component
public class AuthRules {

    private final UserRepository userRepository;

    public AuthRules(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void emailMustBeUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }
    }
}
