package com.app.chat_app.application.features.friendship.mapper;

import org.springframework.stereotype.Component;

import com.app.chat_app.application.features.presence.service.PresenceStore;
import com.app.chat_app.domain.entity.Friendship;
import com.app.chat_app.domain.entity.User;

@Component
public class FriendshipMapper {

    private final PresenceStore presenceStore;

    public FriendshipMapper(PresenceStore presenceStore) {
        this.presenceStore = presenceStore;
    }

    public FriendshipDto toDto(Friendship f) {
        return new FriendshipDto(
                f.getId(),
                f.getRequester().getId(),
                f.getRequester().fullName(),
                f.getRequester().getFriendCode(),
                f.getAddressee().getId(),
                f.getAddressee().fullName(),
                f.getAddressee().getFriendCode(),
                f.getStatus(),
                f.getCreatedAt(),
                f.getRespondedAt()
        );
    }

    /** Mevcut kullanıcı açısından "diğer taraf" kimse onu döner. */
    public FriendDto toFriendDto(Friendship f, java.util.UUID currentUserId) {
        User other = f.getRequester().getId().equals(currentUserId)
                ? f.getAddressee()
                : f.getRequester();

        return new FriendDto(
                other.getId(),
                other.getFirstName(),
                other.getLastName(),
                other.getFriendCode(),
                presenceStore.isOnline(other.getId()),
                other.getLastSeenAt()
        );
    }
}
