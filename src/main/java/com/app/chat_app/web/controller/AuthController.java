package com.app.chat_app.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.chat_app.application.features.auth.command.LoginCommand;
import com.app.chat_app.application.features.auth.command.LogoutCommand;
import com.app.chat_app.application.features.auth.command.RefreshTokenCommand;
import com.app.chat_app.application.features.auth.command.RegisterUserCommand;
import com.app.chat_app.application.features.auth.mapper.AuthResponse;
import com.app.chat_app.application.features.auth.mapper.UserDto;
import com.app.chat_app.application.features.auth.query.GetCurrentUserQuery;
import com.app.chat_app.core.mediator.Mediator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Kullanıcı kayıt, giriş, token yenileme ve oturum işlemleri")
public class AuthController {

    private final Mediator mediator;

    public AuthController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping("/register")
    @Operation(summary = "Yeni kullanıcı kaydı",
               description = "Email + ad/soyad + şifre ile hesap açar, otomatik login olur ve token döner.")
    @SecurityRequirements // public — auth gerektirmez
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/login")
    @Operation(summary = "Email + şifre ile giriş",
               description = "Başarılıysa access + refresh token döner.")
    @SecurityRequirements // public
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access token yenileme",
               description = "Refresh token ile yeni access + yeni refresh döner (rotation). Eski refresh geçersiz olur.")
    @SecurityRequirements // public — access expired olsa da çağrılır
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış",
               description = "Refresh token DB'den silinir. Access token expire olana kadar yaşamaya devam eder; client kendi storage'ını temizlemeli.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout() {
        mediator.send(new LogoutCommand());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Mevcut kullanıcının profili",
               description = "Token'daki userId ile DB'den güncel kullanıcı bilgisini çeker.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> me() {
        return ResponseEntity.ok(mediator.send(new GetCurrentUserQuery()));
    }
}
