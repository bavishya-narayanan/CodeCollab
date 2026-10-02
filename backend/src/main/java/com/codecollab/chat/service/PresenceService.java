package com.codecollab.chat.service;

import java.util.Set;

public interface PresenceService {

    /** Mark user online in a room; TTL extended on subsequent calls. */
    void userJoined(Long roomId, Long userId, String username);

    /** Mark user offline in a room. */
    void userLeft(Long roomId, Long userId, String username);

    /** How many users online in this room. */
    long countOnline(Long roomId);

    /** Is the user currently online in this room? */
    boolean isOnline(Long roomId, Long userId);

    /** All currently online user ids in a room. */
    Set<Long> onlineUsers(Long roomId);

    /** Refresh TTL without broadcasting. */
    void refresh(Long roomId, Long userId, String username);
}
