package com.app.chat_app.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * REST endpoint'leri için global CORS ayarı.
 * WebSocket endpoint'inin CORS'u WebSocketConfiguration'da ayrıca yönetiliyor
 * (setAllowedOriginPatterns).
 *
 * Production'da allowedOrigins listesini daralt:
 *   registry.addMapping("/**").allowedOrigins("https://your-frontend.com")
 */
@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);

                // Swagger UI için
                registry.addMapping("/v3/api-docs/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET")
                        .maxAge(3600);
            }
        };
    }
}
