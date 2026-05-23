package com.app.chat_app.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com.app.chat_app.core.security.encryption.MessageContentConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

@Entity
@Table(
    name = "messages",
    indexes = {
        // En kritik index: bir sohbetin mesajlarını zaman sırasına göre listelemek.
        @Index(name = "ix_messages_chat_sent", columnList = "chat_id, sent_at"),
        @Index(name = "ix_messages_sender", columnList = "sender_id")
    }
)
public class Message {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    /** Sender SYSTEM mesajları için null olabilir; normal mesajda zorunlu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private MessageType type;

    // DB'de AES-256-GCM şifreli saklanır: base64(iv):base64(ciphertext+tag)
    // Plaintext max 4000 char → şifreli çıktı ~5600 char → TEXT tipine bırakıyoruz (length sınırı yok)
    @Convert(converter = MessageContentConverter.class)
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    /** Düzenlendiyse son düzenleme zamanı; aksi halde null. */
    @Column(name = "edited_at")
    private Instant editedAt;

    /** Soft delete: doluysa mesaj silinmiş demektir; içerik UI'da "silindi" gösterilir. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Message() {
        // JPA için
    }

    private Message(Chat chat, User sender, MessageType type, String content) {
        this.id = UUID.randomUUID();
        this.chat = chat;
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    public static Message text(Chat chat, User sender, String content) {
        requireSender(sender);
        requireContent(content);
        return new Message(chat, sender, MessageType.TEXT, content);
    }

    public static Message image(Chat chat, User sender, String contentOrCaption) {
        requireSender(sender);
        return new Message(chat, sender, MessageType.IMAGE, contentOrCaption == null ? "" : contentOrCaption);
    }

    public static Message file(Chat chat, User sender, String contentOrCaption) {
        requireSender(sender);
        return new Message(chat, sender, MessageType.FILE, contentOrCaption == null ? "" : contentOrCaption);
    }

    public static Message system(Chat chat, String content) {
        requireContent(content);
        return new Message(chat, null, MessageType.SYSTEM, content);
    }

    public void edit(String newContent) {
        if (type == MessageType.SYSTEM) {
            throw new IllegalStateException("Sistem mesajları düzenlenemez.");
        }
        if (deletedAt != null) {
            throw new IllegalStateException("Silinmiş mesaj düzenlenemez.");
        }
        requireContent(newContent);
        this.content = newContent;
        this.editedAt = Instant.now();
    }

    public void softDelete() {
        if (deletedAt != null) return;
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() { return deletedAt != null; }
    public boolean isEdited() { return editedAt != null; }

    @PrePersist
    void onCreate() {
        if (sentAt == null) sentAt = Instant.now();
    }

    private static void requireSender(User sender) {
        if (sender == null) throw new IllegalArgumentException("Sender zorunlu.");
    }

    private static void requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Mesaj içeriği boş olamaz.");
        }
    }

    public UUID getId() { return id; }
    public Chat getChat() { return chat; }
    public User getSender() { return sender; }
    public MessageType getType() { return type; }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
    public Instant getEditedAt() { return editedAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
