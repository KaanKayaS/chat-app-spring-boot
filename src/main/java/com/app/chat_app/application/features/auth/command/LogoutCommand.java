package com.app.chat_app.application.features.auth.command;

import com.app.chat_app.core.mediator.cqrs.Command;
import com.app.chat_app.core.security.authorization.AuthorizableRequest;

/**
 * Mevcut kullanıcının refresh token'ını DB'den siler — pratikte "logout".
 * Access token JWT olduğu için server-side iptal edilemez, expire olana kadar yaşar
 * (genelde 15 dk, kabul edilebilir). Refresh token'ın iptali kalıcı çıkış demek.
 */
public record LogoutCommand() implements Command<Void>, AuthorizableRequest { }
