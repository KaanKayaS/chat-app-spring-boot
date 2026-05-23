package com.app.chat_app.web.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.chat_app.application.features.chat.command.CreateGroupChatCommand;
import com.app.chat_app.application.features.chat.command.CreatePrivateChatCommand;
import com.app.chat_app.application.features.chat.command.LeaveGroupChatCommand;
import com.app.chat_app.application.features.chat.mapper.ChatDto;
import com.app.chat_app.application.features.chat.query.GetUserChatsQuery;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.Mediator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chats", description = "Sohbet oluşturma ve listeleme")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final Mediator mediator;

    public ChatController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping("/private")
    @Operation(summary = "Private chat aç", description = "Arkadaşla birebir sohbet. Zaten varsa var olanı döner.")
    public ResponseEntity<ChatDto> openPrivate(@Valid @RequestBody CreatePrivateChatCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/group")
    @Operation(summary = "Grup sohbeti oluştur")
    public ResponseEntity<ChatDto> createGroup(@Valid @RequestBody CreateGroupChatCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @GetMapping
    @Operation(summary = "Sohbet listem", description = "Kullanıcının aktif üye olduğu sohbetler. Sayfalı.")
    public ResponseEntity<PagedResponse<ChatDto>> myChats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediator.send(new GetUserChatsQuery(page, size)));
    }

    @DeleteMapping("/{chatId}/leave")
    @Operation(summary = "Gruptan ayrıl",
               description = "Grup sahibiyseniz ve başka üye varsa önce sahipliği devredin.")
    public ResponseEntity<Void> leave(@PathVariable UUID chatId) {
        mediator.send(new LeaveGroupChatCommand(chatId));
        return ResponseEntity.noContent().build();
    }
}
