package com.app.chat_app.application.features.friendship.rule;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.app.chat_app.core.exception.type.BusinessException;
import com.app.chat_app.domain.entity.FriendshipStatus;
import com.app.chat_app.persistence.repository.FriendshipRepository;

@Component
public class FriendshipRules {

    private final FriendshipRepository friendshipRepository;

    public FriendshipRules(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    public void cannotSendToSelf(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "SELF_FRIEND_REQUEST", "Kendine arkadaşlık isteği gönderemezsin.");
        }
    }

    public void mustNotHaveActiveFriendship(UUID userAId, UUID userBId) {
        friendshipRepository.findBetween(userAId, userBId).ifPresent(existing -> {
            String msg = switch (existing.getStatus()) {
                case PENDING   -> "Zaten bekleyen bir arkadaşlık isteği var.";
                case ACCEPTED  -> "Zaten arkadaşsınız.";
                case BLOCKED   -> "Bu kullanıcıyla arkadaşlık işlemi yapılamaz.";
                case REJECTED  -> null; // reddedilmişse tekrar istek atılabilir
            };
            if (msg != null) {
                throw new BusinessException(HttpStatus.CONFLICT, "FRIENDSHIP_EXISTS", msg);
            }
        });
    }

    public void mustBeAddresseeToRespond(UUID currentUserId, UUID addresseeId) {
        if (!currentUserId.equals(addresseeId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "NOT_ADDRESSEE", "Sadece isteği alan kişi kabul/reddedebiir.");
        }
    }

    public void mustBeAccepted(FriendshipStatus status) {
        if (status != FriendshipStatus.ACCEPTED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "NOT_FRIENDS", "Bu işlem için arkadaş olmanız gerekiyor.");
        }
    }
}
