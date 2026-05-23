package com.app.chat_app.application.features.auth.command;

import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.core.mediator.cqrs.Command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserCommand(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 1, max = 64) String firstName,
        @NotBlank @Size(min = 1, max = 64) String lastName,
        @NotBlank @Size(min = 8, max = 128) String password
) implements Command<AuthResponse> { }
