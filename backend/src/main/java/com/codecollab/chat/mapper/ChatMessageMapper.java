package com.codecollab.chat.mapper;

import com.codecollab.chat.dto.ChatMessageResponse;
import com.codecollab.chat.entity.ChatMessage;
import com.codecollab.user.entity.User;
import com.codecollab.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageMapper {

    private final UserRepository userRepository;

    public ChatMessageResponse toResponse(ChatMessage m) {
        String senderName = userRepository.findById(m.getSenderId())
            .map(User::getUsername).orElse("[unknown]");
        String recipientName = (m.getRecipientId() != null)
            ? userRepository.findById(m.getRecipientId())
                .map(User::getUsername).orElse("[unknown]")
            : null;
        return new ChatMessageResponse(
            m.getId(), m.getRoomId(), m.getSenderId(), senderName,
            m.getRecipientId(), recipientName, m.getMessageType(),
            m.getContent(), m.isDeleted(), m.getCreatedAt()
        );
    }
}
