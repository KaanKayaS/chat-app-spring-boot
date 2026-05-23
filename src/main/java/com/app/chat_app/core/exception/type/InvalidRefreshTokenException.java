package com.app.chat_app.core.exception.type;

import org.springframework.http.HttpStatus;

/**
 * 401 — Refresh token bulunamadı, süresi dolmuş veya iptal edilmiş.
 * Frontend bunu görünce kullanıcıyı login sayfasına yönlendirmeli.
 */
public class InvalidRefreshTokenException extends BusinessException {

    public static final String CODE = "INVALID_REFRESH_TOKEN";

    public InvalidRefreshTokenException() {
        this("Refresh token geçersiz veya süresi dolmuş. Lütfen tekrar giriş yap.");
    }

    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, CODE, message);
    }
}
