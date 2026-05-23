package com.app.chat_app.domain.entity;

public enum FriendshipStatus {
    /** İstek atıldı, addressee henüz cevap vermedi. */
    PENDING,

    /** addressee isteği kabul etti — taraflar artık arkadaş. */
    ACCEPTED,

    /** addressee isteği reddetti. Yeniden istek atılabilir mi, business kararı. */
    REJECTED,

    /** Taraflardan biri diğerini engelledi. */
    BLOCKED
}
