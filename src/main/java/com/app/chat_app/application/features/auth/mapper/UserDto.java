package com.app.chat_app.application.features.auth.mapper;

import java.util.UUID;

/**
 * Frontend'e döndürdüğümüz "kullanıcı profili" görünümü.
 * passwordHash, refreshToken gibi hassas alanları kapsam dışı bırakıyoruz.
 */
public record UserDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String friendCode,
        boolean emailConfirmed
) { }
