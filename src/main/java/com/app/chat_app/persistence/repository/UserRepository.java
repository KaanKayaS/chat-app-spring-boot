package com.app.chat_app.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.chat_app.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFriendCode(String friendCode);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    boolean existsByFriendCode(String friendCode);
}
