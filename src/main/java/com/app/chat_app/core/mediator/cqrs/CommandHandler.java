package com.app.chat_app.core.mediator.cqrs;

public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
