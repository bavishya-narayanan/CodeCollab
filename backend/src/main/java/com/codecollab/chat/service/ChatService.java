package com.codecollab.chat.service;

import com.codecollab.chat.dto.ChatMessageRequest;
import com.codecollab.chat.dto.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    ChatMessageResponse sendToRoom(Long roomId, Long senderId, ChatMessageRequest req);

    ChatMessageResponse sendDirect(Long recipientId, Long senderId, ChatMessageRequest req);

    Page<ChatMessageResponse> history(Long roomId, Pageable pageable);

    Page<ChatMessageResponse> directHistory(Long userId, Long otherUserId, Pageable pageable);

    void deleteMessage(Long messageId, Long actorId, boolean isAdmin);
}
