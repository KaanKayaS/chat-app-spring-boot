package com.app.chat_app.application.features.auth.command;

import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.core.mediator.cqrs.Command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank @Email String email,
        @NotBlank String password
) implements Command<AuthResponse> { }
