package com.app.chat_app.core.mediator.cqrs;

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
