package com.app.chat_app.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Hem birebir özel sohbet (PRIVATE) hem grup sohbeti (GROUP) bu entity ile temsil edilir.
 * Tip ayrımına göre bazı alanlar anlamlı/anlamsız olur:
 *   - PRIVATE: name, creator, avatarUrl null kalır.
 *   - GROUP:   name zorunlu, creator zorunlu, avatarUrl opsiyonel.
 * Bu kural application/service katmanında kontrol edilir (DB seviyesi ayrım yapmıyor).
 */
@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private ChatType type;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Chat() {
        // JPA için
    }

    private Chat(ChatType type, String name, User creator) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.name = name;
        this.creator = creator;
    }

    public static Chat privateChat() {
        return new Chat(ChatType.PRIVATE, null, null);
    }

    public static Chat group(String name, User creator) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Grubun adı boş olamaz.");
        }
        if (creator == null) {
            throw new IllegalArgumentException("Grubun yaratıcısı zorunludur.");
        }
        return new Chat(ChatType.GROUP, name, creator);
    }

    public void rename(String newName) {
        if (type != ChatType.GROUP) {
            throw new IllegalStateException("Sadece grup sohbetlerinin adı değiştirilebilir.");
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Grup adı boş olamaz.");
        }
        this.name = newName;
    }

    public void changeAvatar(String avatarUrl) {
        if (type != ChatType.GROUP) {
            throw new IllegalStateException("Sadece grup sohbetlerinin avatarı değiştirilebilir.");
        }
        this.avatarUrl = avatarUrl;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public ChatType getType() { return type; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public User getCreator() { return creator; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isGroup() { return type == ChatType.GROUP; }
    public boolean isPrivate() { return type == ChatType.PRIVATE; }
}
