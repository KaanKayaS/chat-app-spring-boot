package com.app.chat_app.domain.entity;

public enum ParticipantRole {
    /** Grubun sahibi. Genelde tek kişi, grubu silebilen kişidir. */
    OWNER,

    /** Yönetici. Üye ekleyip çıkarabilir; sahibi değiştiremez. */
    ADMIN,

    /** Normal üye. Sadece mesaj atar/okur. */
    MEMBER
}
