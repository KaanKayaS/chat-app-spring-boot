package com.app.chat_app.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.chat_app.domain.entity.ChatParticipant;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, UUID> {

    /** Kullanıcının bu sohbetteki üyeliği (yetki/okundu bilgisi için). */
    Optional<ChatParticipant> findByChatIdAndUserId(UUID chatId, UUID userId);

    /** Authorization kontrolünde sıkça lazım — üye mi değil mi. */
    boolean existsByChatIdAndUserIdAndLeftAtIsNull(UUID chatId, UUID userId);

    /** Bir sohbetin aktif üyeleri (gruptan ayrılanlar dahil değil). */
    List<ChatParticipant> findByChatIdAndLeftAtIsNull(UUID chatId);

    /** Kullanıcının aktif olduğu tüm üyelikler. */
    List<ChatParticipant> findByUserIdAndLeftAtIsNull(UUID userId);

    /** Aktif üye sayısı — grup için 1+ aktif kalıp kalmadığını anlamak için. */
    long countByChatIdAndLeftAtIsNull(UUID chatId);
}
