package com.app.chat_app.application.features.presence.command;

import java.util.UUID;

import com.app.chat_app.core.mediator.cqrs.Command;

public record SetUserOnlineCommand(UUID userId, String sessionId) implements Command<Void> { }
