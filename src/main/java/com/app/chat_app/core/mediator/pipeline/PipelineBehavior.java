package com.app.chat_app.core.mediator.pipeline;

public interface PipelineBehavior {
    <R> R handle(Object request, RequestHandlerDelegate<R> next);

    default boolean supports(Object request) {
        return true; 
    }
}
