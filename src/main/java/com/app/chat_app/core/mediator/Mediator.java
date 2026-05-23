package com.app.chat_app.core.mediator;

import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.mediator.cqrs.Query;

public interface Mediator {
  <R> R send(Command<R> command);
  <R> R send(Query<R> query);
}
