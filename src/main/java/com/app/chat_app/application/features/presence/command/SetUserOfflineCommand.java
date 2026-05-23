package com.app.chat_app.application.features.presence.command;

import com.app.chat_app.core.mediator.cqrs.Command;

public record SetUserOfflineCommand(String sessionId) implements Command<Void> { }
