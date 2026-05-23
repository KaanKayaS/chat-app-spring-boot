package com.app.chat_app.application.features.auth.mapper;

import java.time.Instant;

/**
 * Login / Register / Refresh akışlarının ortak cevap formatı.
 * - accessToken: Authorization: Bearer ... olarak gönderilir
 * - refreshToken: access süresi dolunca /auth/refresh çağırılırken kullanılır
 * - refreshTokenExpiresAt: frontend storage'da takip için (UTC instant)
 */
public record AuthResponse(
        UserDto user,
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt
) { }
