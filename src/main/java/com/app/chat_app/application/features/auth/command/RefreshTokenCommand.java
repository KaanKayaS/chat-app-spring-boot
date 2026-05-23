package com.app.chat_app.application.features.auth.command;

import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.core.mediator.cqrs.Command;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(
        @NotBlank String refreshToken
) implements Command<AuthResponse> { }
