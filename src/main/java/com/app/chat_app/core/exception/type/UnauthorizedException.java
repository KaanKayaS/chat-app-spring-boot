package com.app.chat_app.core.exception.type;

import org.springframework.http.HttpStatus;

/**
 * 403 — Kimliği biliyorum ama bu işlemi yapma yetkin yok.
 * (Yanlış rol, başkasının kaynağına erişmeye çalışmak vb.)
 */
public class UnauthorizedException extends BusinessException {

    public static final String CODE = "UNAUTHORIZED";

    public UnauthorizedException() {
        this("Bu işlem için yetkin yok.");
    }

    public UnauthorizedException(String message) {
        super(HttpStatus.FORBIDDEN, CODE, message);
    }
}
