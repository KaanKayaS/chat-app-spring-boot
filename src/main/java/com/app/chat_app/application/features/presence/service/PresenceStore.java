package com.app.chat_app.application.features.presence.service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Şu an online olan kullanıcıların in-memory kaydı.
 *
 * Tek sunucu için yeterli. Çoklu sunucu / pod durumunda burası
 * Redis Set'e taşınır (değişmesi gereken tek yer).
 *
 * Thread-safe: ConcurrentHashSet (ConcurrentHashMap.newKeySet()).
 */
@Component
public class PresenceStore {

    // sessionId → userId: bir kullanıcının birden fazla sekmesi/cihazı olabilir
    private final ConcurrentHashMap<String, UUID> sessionToUser = new ConcurrentHashMap<>();

    public void connect(String sessionId, UUID userId) {
        sessionToUser.put(sessionId, userId);
    }

    public UUID disconnect(String sessionId) {
        return sessionToUser.remove(sessionId);
    }

    public boolean isOnline(UUID userId) {
        return sessionToUser.containsValue(userId);
    }

    public Set<UUID> onlineUserIds() {
        Set<UUID> result = ConcurrentHashMap.newKeySet(sessionToUser.size());
        result.addAll(sessionToUser.values());
        return Collections.unmodifiableSet(result);
    }

    /**
     * Verilen kullanıcı kümesinden online olanları döner.
     * Arkadaş listesinde online olanları filtrelemek için.
     */
    public Set<UUID> filterOnline(Set<UUID> userIds) {
        Set<UUID> result = ConcurrentHashMap.newKeySet();
        for (UUID id : userIds) {
            if (isOnline(id)) result.add(id);
        }
        return Collections.unmodifiableSet(result);
    }
}
