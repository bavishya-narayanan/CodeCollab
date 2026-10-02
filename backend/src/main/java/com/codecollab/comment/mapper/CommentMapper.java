package com.codecollab.comment.mapper;

import com.codecollab.comment.dto.CommentResponse;
import com.codecollab.comment.entity.Comment;
import com.codecollab.user.entity.User;
import com.codecollab.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentResponse toResponse(Comment c, long replyCount) {
        String username = resolveUsername(c.getUserId());
        boolean edited = c.getUpdatedAt() != null 
            && c.getCreatedAt() != null 
            && c.getUpdatedAt().isAfter(c.getCreatedAt().plusSeconds(1));
            
        return new CommentResponse(
            c.getId(),
            c.getSnippetId(),
            c.getUserId(),
            username,
            c.getParentId(),
            c.isDeleted() ? "[deleted]" : c.getContent(),
            c.isHelpful(),
            c.isDeleted(),
            edited,
            c.getCreatedAt(),
            c.getUpdatedAt(),
            replyCount,
            List.of()
        );
    }

    private String resolveUsername(Long userId) {
        return userRepository.findById(userId)
            .map(User::getUsername)
            .orElse("[unknown]");
    }
}
