package com.app.chat_app.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * "bearerAuth" security scheme tanımlıyoruz; Swagger UI'da sağ üstteki Authorize
 * düğmesine basıp access token yazınca, korumalı endpoint'lere otomatik olarak
 * `Authorization: Bearer <token>` header'ı eklenir.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI chatAppOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT access token. Login/Register response'undan alıp buraya yapıştır.");

        return new OpenAPI()
                .info(new Info()
                        .title("Chat App API")
                        .description("Gerçek zamanlı mesajlaşma uygulaması — auth, friendship, chat ve message endpoint'leri.")
                        .version("v0.0.1")
                        .contact(new Contact().name("Chat App")))
                // Global default: tüm endpoint'lerde bearer auth görünür. Public olanlarda
                // (login, register, refresh) controller seviyesinde @SecurityRequirements ile kapatabiliriz.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerScheme));
    }
}
