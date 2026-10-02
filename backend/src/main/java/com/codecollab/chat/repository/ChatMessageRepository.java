package com.codecollab.chat.repository;

import com.codecollab.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Room history (paged, newest first)
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.roomId = :roomId AND m.deleted = FALSE
        ORDER BY m.createdAt DESC
        """)
    Page<ChatMessage> findHistoryForRoom(@Param("roomId") Long roomId, 
                                          Pageable pageable);

    // Direct message history between two users (both directions)
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.deleted = FALSE
          AND ((m.senderId = :a AND m.recipientId = :b)
            OR (m.senderId = :b AND m.recipientId = :a))
        ORDER BY m.createdAt DESC
        """)
    Page<ChatMessage> findDirectHistory(@Param("a") Long userA,
                                         @Param("b") Long userB,
                                         Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deleted = TRUE, " +
           "m.content = '[deleted]' WHERE m.id = :id")
    void softDelete(@Param("id") Long id);
}
