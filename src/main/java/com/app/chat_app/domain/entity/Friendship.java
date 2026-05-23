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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "friendships",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_friendships_pair",
        columnNames = {"requester_id", "addressee_id"}
    )
)
public class Friendship {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private FriendshipStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    protected Friendship() {
        // JPA için
    }

    public Friendship(User requester, User addressee) {
        if (requester.getId().equals(addressee.getId())) {
            throw new IllegalArgumentException("Kendine arkadaşlık isteği gönderemezsin.");
        }
        this.id = UUID.randomUUID();
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }

    public void accept() {
        ensurePending();
        this.status = FriendshipStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    public void reject() {
        ensurePending();
        this.status = FriendshipStatus.REJECTED;
        this.respondedAt = Instant.now();
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
        this.respondedAt = Instant.now();
    }

    public boolean involves(UUID userId) {
        return requester.getId().equals(userId) || addressee.getId().equals(userId);
    }

    private void ensurePending() {
        if (status != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Sadece PENDING durumdaki istek için bu işlem yapılabilir.");
        }
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getRequester() { return requester; }
    public User getAddressee() { return addressee; }
    public FriendshipStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }
}
