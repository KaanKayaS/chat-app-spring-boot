package com.app.chat_app.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.chat_app.domain.entity.Friendship;
import com.app.chat_app.domain.entity.FriendshipStatus;
import com.app.chat_app.domain.entity.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    /**
     * İki kullanıcı arasındaki arkadaşlığı yön bağımsız bulur.
     * Yeni istek atılırken duplicate kontrolü için kullanılır.
     */
    @Query("""
           SELECT f FROM Friendship f
           WHERE (f.requester.id = :userAId AND f.addressee.id = :userBId)
              OR (f.requester.id = :userBId AND f.addressee.id = :userAId)
           """)
    Optional<Friendship> findBetween(@Param("userAId") UUID userAId,
                                     @Param("userBId") UUID userBId);

    /** Kullanıcıya gelen istekler — sayfalı. */
    Page<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status, Pageable pageable);

    /** Kullanıcının gönderdiği istekler — sayfalı. */
    Page<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status, Pageable pageable);

    /**
     * Kabul edilmiş arkadaşlıklar — sayfalı.
     * countQuery ayrı tutuldu: JOIN olmadan count daha hızlı.
     */
    @Query(
        value = """
                SELECT f FROM Friendship f
                WHERE f.status = com.app.chat_app.domain.entity.FriendshipStatus.ACCEPTED
                  AND (f.requester.id = :userId OR f.addressee.id = :userId)
                """,
        countQuery = """
                SELECT COUNT(f) FROM Friendship f
                WHERE f.status = com.app.chat_app.domain.entity.FriendshipStatus.ACCEPTED
                  AND (f.requester.id = :userId OR f.addressee.id = :userId)
                """
    )
    Page<Friendship> findAcceptedFriendships(@Param("userId") UUID userId, Pageable pageable);

    /** Eski List versiyonu — hâlâ BlockUserCommandHandler ihtiyacı var. */
    @Query("""
           SELECT f FROM Friendship f
           WHERE f.status = com.app.chat_app.domain.entity.FriendshipStatus.ACCEPTED
             AND (f.requester.id = :userId OR f.addressee.id = :userId)
           """)
    List<Friendship> findAcceptedFriendships(@Param("userId") UUID userId);
}
