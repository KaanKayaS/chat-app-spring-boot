package com.app.chat_app.core.exception.type;

import org.springframework.http.HttpStatus;

/**
 * 401 — Email veya şifre yanlış.
 *
 * NOT: Mesajda hangisinin yanlış olduğunu BELLİ ETMEYİZ — "email yanlış" demek
 * saldırgana hangi email'in kayıtlı olup olmadığını sızdırır (user enumeration).
 * Bu yüzden hem email-yok hem şifre-yanlış için aynı exception ve aynı mesaj atılır.
 */
public class InvalidCredentialsException extends BusinessException {

    public static final String CODE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, CODE, "Email veya şifre hatalı.");
    }
}
