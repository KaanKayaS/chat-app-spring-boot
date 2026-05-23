package com.app.chat_app.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Bir kullanıcının bir sohbete üyeliği. Hem PRIVATE hem GROUP için kullanılır.
 * PRIVATE'ta tam olarak 2 satır olur, role her zaman MEMBER.
 * GROUP'ta 1 OWNER + N MEMBER/ADMIN olur.
 *
 * Okundu bilgisi `lastReadMessageId` ile tutuluyor — bu, kullanıcının bu sohbette
 * en son okuduğu mesajın id'si. Unread count = bu id'den sonraki mesaj sayısı.
 */
@Entity
@Table(
    name = "chat_participants",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_chat_participants_chat_user",
        columnNames = {"chat_id", "user_id"}
    ),
    indexes = {
        @Index(name = "ix_chat_participants_user", columnList = "user_id"),
        @Index(name = "ix_chat_participants_chat", columnList = "chat_id")
    }
)
public class ChatParticipant {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    /** null ise hâlâ üye; doluysa gruptan ayrıldı/atıldı. */
    @Column(name = "left_at")
    private Instant leftAt;

    /** Bu kullanıcının bu sohbette en son okuduğu mesajın id'si. */
    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    protected ChatParticipant() {
        // JPA için
    }

    public ChatParticipant(Chat chat, User user, ParticipantRole role) {
        this.id = UUID.randomUUID();
        this.chat = chat;
        this.user = user;
        this.role = role;
    }

    public void markRead(UUID messageId) {
        this.lastReadMessageId = messageId;
    }

    public void leave() {
        this.leftAt = Instant.now();
    }

    public void promoteToAdmin() {
        if (role == ParticipantRole.OWNER) return;
        this.role = ParticipantRole.ADMIN;
    }

    public void demoteToMember() {
        if (role == ParticipantRole.OWNER) {
            throw new IllegalStateException("OWNER'ı demote edemezsin, önce ownership transfer et.");
        }
        this.role = ParticipantRole.MEMBER;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    @PrePersist
    void onCreate() {
        if (joinedAt == null) joinedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Chat getChat() { return chat; }
    public User getUser() { return user; }
    public ParticipantRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
    public Instant getLeftAt() { return leftAt; }
    public UUID getLastReadMessageId() { return lastReadMessageId; }
}
