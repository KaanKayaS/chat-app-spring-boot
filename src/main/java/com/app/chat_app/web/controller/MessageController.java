package com.app.chat_app.web.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.chat_app.application.features.chat.command.DeleteMessageCommand;
import com.app.chat_app.application.features.chat.command.EditMessageCommand;
import com.app.chat_app.application.features.chat.command.SendMessageCommand;
import com.app.chat_app.application.features.chat.mapper.MessageDto;
import com.app.chat_app.application.features.chat.query.GetChatMessagesQuery;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.Mediator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Mesaj gönderme, düzenleme, silme ve listeleme")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final Mediator mediator;

    public MessageController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping
    @Operation(summary = "Mesaj gönder", description = "Sohbete mesaj atar, WebSocket üzerinden anında broadcast edilir.")
    public ResponseEntity<MessageDto> send(@Valid @RequestBody SendMessageCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Mesaj geçmişi", description = "Yeniden eskiye sayfalı. page=0, size=50 en son 50 mesaj.")
    public ResponseEntity<PagedResponse<MessageDto>> list(
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(mediator.send(new GetChatMessagesQuery(chatId, page, size)));
    }

    @PutMapping("/{messageId}")
    @Operation(summary = "Mesajı düzenle", description = "Sadece kendi mesajını düzenleyebilirsin.")
    public ResponseEntity<MessageDto> edit(
            @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageCommand command) {
        // PathVariable ile body'deki ID'nin eşleşip eşleşmediğini kontrol et
        var effectiveCommand = new EditMessageCommand(messageId, command.newContent());
        return ResponseEntity.ok(mediator.send(effectiveCommand));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Mesajı sil", description = "Soft delete — içerik 'silindi' olarak gösterilir.")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        mediator.send(new DeleteMessageCommand(messageId));
        return ResponseEntity.noContent().build();
    }
}
