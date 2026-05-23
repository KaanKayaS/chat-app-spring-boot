package com.app.chat_app.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.chat_app.application.features.friendship.command.BlockUserCommand;
import com.app.chat_app.application.features.friendship.command.RespondFriendRequestCommand;
import com.app.chat_app.application.features.friendship.command.SendFriendRequestCommand;
import com.app.chat_app.application.features.friendship.mapper.FriendDto;
import com.app.chat_app.application.features.friendship.mapper.FriendshipDto;
import com.app.chat_app.application.features.friendship.query.GetFriendRequestsQuery;
import com.app.chat_app.application.features.friendship.query.GetFriendsQuery;
import com.app.chat_app.core.dto.PagedResponse;
import com.app.chat_app.core.mediator.Mediator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/friendships")
@Tag(name = "Friendships", description = "Arkadaşlık isteği gönderme, kabul/reddetme, engelleme ve listeleme")
@SecurityRequirement(name = "bearerAuth")
public class FriendshipController {

    private final Mediator mediator;

    public FriendshipController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping("/request")
    @Operation(summary = "Arkadaşlık isteği gönder", description = "Friend code ile istek at.")
    public ResponseEntity<FriendshipDto> sendRequest(@Valid @RequestBody SendFriendRequestCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/respond")
    @Operation(summary = "İsteği kabul et veya reddet")
    public ResponseEntity<FriendshipDto> respond(@Valid @RequestBody RespondFriendRequestCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/block")
    @Operation(summary = "Kullanıcıyı engelle")
    public ResponseEntity<FriendshipDto> block(@Valid @RequestBody BlockUserCommand command) {
        return ResponseEntity.ok(mediator.send(command));
    }

    @GetMapping
    @Operation(summary = "Arkadaş listesi", description = "ACCEPTED durumundaki arkadaşlar, online bilgisiyle. Sayfalı.")
    public ResponseEntity<PagedResponse<FriendDto>> getFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediator.send(new GetFriendsQuery(page, size)));
    }

    @GetMapping("/requests")
    @Operation(summary = "Gelen arkadaşlık istekleri", description = "PENDING durumundaki bekleyen istekler. Sayfalı.")
    public ResponseEntity<PagedResponse<FriendshipDto>> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediator.send(new GetFriendRequestsQuery(page, size)));
    }
}
