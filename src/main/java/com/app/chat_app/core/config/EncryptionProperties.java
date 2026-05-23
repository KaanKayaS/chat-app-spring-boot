package com.app.chat_app.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml'daki encryption bloğuna karşılık geliyor.
 *
 * aesKey: Base64 ile encode edilmiş 256-bit (32 byte) AES anahtarı.
 *
 * Yeni anahtar üretmek için:
 *   openssl rand -base64 32
 * veya Java'da:
 *   Base64.getEncoder().encodeToString(KeyGenerator.getInstance("AES").generateKey().getEncoded())
 *
 * PRODUCTION'DA:
 *   Bu değeri asla kaynak koduna commit etme.
 *   Env variable veya secrets manager (Vault, AWS Secrets Manager) kullan:
 *   encryption.aes-key=${ENCRYPTION_AES_KEY}
 */
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {

    private String aesKey;

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}
