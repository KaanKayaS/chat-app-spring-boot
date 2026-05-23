package com.app.chat_app.application.features.auth.query;

import com.app.chat_app.application.features.auth.mapper.UserDto;
import com.app.chat_app.core.mediator.cqrs.Query;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

/**
 * "Şu an giriş yapmış kullanıcı kim?" — frontend açılırken çağırır
 * (token'dan email/userId okumadan, güncel user bilgisini DB'den almak için).
 */
public record GetCurrentUserQuery() implements Query<UserDto>, AuthorizableRequest { }
