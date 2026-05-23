package com.app.chat_app.web.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * STOMP topic yapısı:
 *   /topic/chat.{chatId}         → bir sohbetin yeni/güncellenen mesajları
 *   /topic/chat.{chatId}.read    → okundu bildirimleri
 *   /topic/presence              → online/offline değişimleri
 *   /user/queue/notifications    → kişiye özel bildirimler (arkadaşlık isteği vb.)
 *
 * Client WebSocket endpoint'i: ws://localhost:8080/ws
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfiguration(StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new WebSocketAuthInterceptor())
                .setAllowedOriginPatterns("*");
    }

    /**
     * Gelen STOMP mesajlarını işleyen kanalın interceptor'ları.
     * StompAuthChannelInterceptor burada devreye girerek CONNECT frame'inde
     * JWT'den Principal oluşturur — tüm @MessageMapping metotlarına Principal enjekte olur.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024);
        registration.setSendBufferSizeLimit(512 * 1024);
    }
}
