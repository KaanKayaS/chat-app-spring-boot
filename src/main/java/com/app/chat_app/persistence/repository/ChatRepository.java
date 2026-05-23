package com.app.chat_app.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.chat_app.domain.entity.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * İki kullanıcı arasındaki PRIVATE chat'i bulur — "find or create" pattern'i için.
     */
    @Query("""
           SELECT c FROM Chat c
           WHERE c.type = com.app.chat_app.domain.entity.ChatType.PRIVATE
             AND EXISTS (SELECT 1 FROM ChatParticipant p1
                         WHERE p1.chat = c AND p1.user.id = :userAId)
             AND EXISTS (SELECT 1 FROM ChatParticipant p2
                         WHERE p2.chat = c AND p2.user.id = :userBId)
           """)
    Optional<Chat> findPrivateChatBetween(@Param("userAId") UUID userAId,
                                          @Param("userBId") UUID userBId);

    /**
     * Kullanıcının üye olduğu sohbetler — sayfalı.
     * countQuery ayrı: JOIN içermeyen basit count daha hızlı.
     */
    @Query(
        value = """
                SELECT c FROM Chat c
                JOIN ChatParticipant p ON p.chat = c
                WHERE p.user.id = :userId AND p.leftAt IS NULL
                """,
        countQuery = """
                SELECT COUNT(c) FROM Chat c
                JOIN ChatParticipant p ON p.chat = c
                WHERE p.user.id = :userId AND p.leftAt IS NULL
                """
    )
    Page<Chat> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
