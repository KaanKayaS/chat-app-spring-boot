package com.app.chat_app.core.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter: Message.content alanını DB'ye yazarken şifreler,
 * DB'den okurken çözer. Handler/entity kodu hiç değişmez — şeffaf çalışır.
 *
 * Spring Boot + Hibernate entegrasyonu sayesinde @Component olmadan da
 * AesEncryptionService inject edilebiliyor (SpringBeanContainer via
 * HibernateJpaAutoConfiguration). autoApply=false — sadece @Convert ile
 * belirtilen alanlara uygulanır, tüm String field'lara değil.
 *
 * @Convert(converter = MessageContentConverter.class) ile entity'de kullanılır.
 */
@Converter(autoApply = false)
public class MessageContentConverter implements AttributeConverter<String, String> {

    private final AesEncryptionService encryptionService;

    public MessageContentConverter(AesEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        return encryptionService.encrypt(plaintext);
    }

    @Override
    public String convertToEntityAttribute(String stored) {
        return encryptionService.decrypt(stored);
    }
}
