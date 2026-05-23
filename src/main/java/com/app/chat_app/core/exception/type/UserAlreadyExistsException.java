package com.app.chat_app.core.exception.type;

import org.springframework.http.HttpStatus;

/**
 * 409 — Aynı email ile zaten kayıtlı bir kullanıcı var.
 */
public class UserAlreadyExistsException extends BusinessException {

    public static final String CODE = "USER_ALREADY_EXISTS";

    public UserAlreadyExistsException() {
        this("Bu email adresi zaten kullanılıyor.");
    }

    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, CODE, message);
    }
}
