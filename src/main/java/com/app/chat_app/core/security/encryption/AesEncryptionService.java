package com.app.chat_app.core.security.encryption;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.app.chat_app.core.config.EncryptionProperties;

/**
 * AES-256-GCM ile şifreleme/çözme.
 *
 * Neden GCM?
 *   - CTR modunun üzerine GMAC authentication tag ekler.
 *   - Ciphertext manipüle edilirse decrypt sırasında AEADBadTagException fırlar.
 *   - CBC'nin padding oracle saldırılarından muaf.
 *
 * DB'de saklanan format:
 *   base64(iv) + ":" + base64(ciphertext + authTag)
 *   Örnek: "abc123==" + ":" + "XYZ+/=="
 *
 * IV (nonce): Her şifrelemede 12 byte rastgele üretilir — aynı mesaj iki kez
 *             şifrelense bile DB'de tamamen farklı görünür (semantik güvenlik).
 */
@Service
@EnableConfigurationProperties(EncryptionProperties.class)
public class AesEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final String DELIMITER = ":";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey secretKey;

    public AesEncryptionService(EncryptionProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.getAesKey());
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES anahtarı 256-bit (32 byte) olmalı. Mevcut: " + keyBytes.length + " byte.");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            String ivB64 = Base64.getEncoder().encodeToString(iv);
            String ciphertextB64 = Base64.getEncoder().encodeToString(ciphertext);
            return ivB64 + DELIMITER + ciphertextB64;
        } catch (Exception e) {
            throw new EncryptionException("Mesaj şifrelenemedi.", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) return null;

        try {
            int delimIndex = stored.indexOf(DELIMITER);
            if (delimIndex < 0) {
                throw new EncryptionException("Geçersiz şifreli veri formatı.");
            }

            byte[] iv = Base64.getDecoder().decode(stored.substring(0, delimIndex));
            byte[] ciphertext = Base64.getDecoder().decode(stored.substring(delimIndex + 1));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptionException("Mesaj çözülemedi. Veri bozulmuş veya anahtar yanlış.", e);
        }
    }
}
