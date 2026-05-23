package com.app.chat_app.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.chat_app.domain.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Bir sohbetin mesaj geçmişi — yeniden eskiye doğru sayfalı (infinite scroll).
     * UI tarafı bunu ters çevirip gösterir.
     */
    Page<Message> findByChatIdOrderBySentAtDesc(UUID chatId, Pageable pageable);

    /**
     * Belirli bir andan sonra atılan mesajları zaman sırasıyla döner.
     * "Kullanıcı bağlandı, son okuduğu mesajdan sonrasını çek" senaryosu için.
     */
    List<Message> findByChatIdAndSentAtAfterOrderBySentAtAsc(UUID chatId, Instant afterSentAt);

    /**
     * Bir kullanıcının bir sohbetteki okunmamış mesaj sayısı.
     * `lastReadMessageId` null ise tüm mesajları okunmamış sayar.
     */
    @Query("""
           SELECT COUNT(m) FROM Message m
           WHERE m.chat.id = :chatId
             AND (
                  :lastReadMessageId IS NULL
                  OR m.sentAt > (SELECT m2.sentAt FROM Message m2 WHERE m2.id = :lastReadMessageId)
             )
           """)
    long countUnread(@Param("chatId") UUID chatId,
                     @Param("lastReadMessageId") UUID lastReadMessageId);
}
