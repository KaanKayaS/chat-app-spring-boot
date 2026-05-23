package com.app.chat_app.web.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket HTTP handshake interceptor'ı.
 *
 * JWT doğrulaması STOMP katmanında (StompAuthChannelInterceptor) yapılıyor.
 * Bu interceptor tüm bağlantılara izin verir; token yoksa anonymous bağlantı olur,
 * Principal set edilmez ve @MessageMapping metotlarında principal null gelir.
 */
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) { }
}
