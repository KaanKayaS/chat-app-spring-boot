package com.app.chat_app.core.exception.type;

import org.springframework.http.HttpStatus;

/**
 * 401 — Kullanıcı kimliği belirlenemedi (token yok, geçersiz, expire).
 * "Kim olduğunu bilmiyorum" durumu.
 */
public class UnauthenticatedException extends BusinessException {

    public static final String CODE = "UNAUTHENTICATED";

    public UnauthenticatedException() {
        this("Giriş yapmalısın.");
    }

    public UnauthenticatedException(String message) {
        super(HttpStatus.UNAUTHORIZED, CODE, message);
    }
}
