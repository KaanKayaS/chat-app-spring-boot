package com.app.chat_app.core.security.encryption;

/**
 * Şifreleme/çözme sırasında oluşan runtime hatası.
 * Kontrol edilmesi gereken checked exception değil — şifreleme altyapı sorunu
 * olduğunda uygulama zaten devam etmemeli.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
