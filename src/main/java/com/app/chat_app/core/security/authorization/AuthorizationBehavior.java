package com.app.chat_app.core.security.authorization;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.app.chat_app.core.exception.type.UnauthenticatedException;
import com.app.chat_app.core.mediator.pipeline.PipelineBehavior;
import com.app.chat_app.core.mediator.pipeline.RequestHandlerDelegate;
import com.app.chat_app.core.security.context.UserContext;

/**
 * AuthorizableRequest işaretli command/query'lerden önce çalışır.
 * Kullanıcı giriş yapmamışsa 401 (UnauthenticatedException) fırlatır.
 *
 * Rol bazlı 403 (UnauthorizedException) ihtiyacı doğunca,
 * AuthorizableRequest'i `requiredRoles()` döndürecek hale genişletip burada
 * userContext.getRoles() ile kontrol ederiz.
 */
@Component
@Order(10)
public class AuthorizationBehavior implements PipelineBehavior {

    private final UserContext userContext;

    public AuthorizationBehavior(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public boolean supports(Object request) {
        return request instanceof AuthorizableRequest;
    }

    @Override
    public <R> R handle(Object request, RequestHandlerDelegate<R> next) {
        if (!userContext.isAuthenticated()) {
            throw new UnauthenticatedException();
        }

        return next.invoke();
    }
}
