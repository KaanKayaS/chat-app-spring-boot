package com.app.chat_app.domain.entity;

public enum MessageType {
    /** Normal yazı mesajı. */
    TEXT,

    /** Görsel ekli (content = caption, ek bir attachment alanı/tablosu istersen sonra ekleriz). */
    IMAGE,

    /** Dosya eki. */
    FILE,

    /** "X kullanıcısı gruba katıldı / Y grubu sildi" gibi sistem mesajları. */
    SYSTEM
}
