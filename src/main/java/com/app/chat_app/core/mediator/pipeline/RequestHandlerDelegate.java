package com.app.chat_app.core.mediator.pipeline;

@FunctionalInterface
public interface RequestHandlerDelegate<R> {
    R invoke();
}