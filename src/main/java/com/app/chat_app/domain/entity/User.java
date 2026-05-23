package com.app.chat_app.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "ix_users_email", columnList = "email", unique = true),
        @Index(name = "ix_users_friend_code", columnList = "friend_code", unique = true)
    }
)
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 256)
    private String email;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 64)
    private String lastName;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Discord/Steam tarzı, arkadaş eklemek için kullanılan kısa kod.
    // Üretimini service katmanı yapar (collision olursa retry).
    @Column(name = "friend_code", nullable = false, length = 16)
    private String friendCode;

    @Column(name = "email_confirmed", nullable = false)
    private boolean emailConfirmed = true;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private Instant refreshTokenExpiresAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected User() {
        // JPA için
    }

    public User(String email, String firstName, String lastName, String passwordHash, String friendCode) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.friendCode = friendCode;
    }

    public void setRefreshToken(String refreshToken, Instant expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }

    public boolean isRefreshTokenValid(String candidate) {
        if (refreshToken == null || refreshTokenExpiresAt == null) return false;
        if (refreshTokenExpiresAt.isBefore(Instant.now())) return false;
        return refreshToken.equals(candidate);
    }

    public void confirmEmail() {
        this.emailConfirmed = true;
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPasswordHash() { return passwordHash; }
    public String getFriendCode() { return friendCode; }
    public boolean isEmailConfirmed() { return emailConfirmed; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getRefreshTokenExpiresAt() { return refreshTokenExpiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
