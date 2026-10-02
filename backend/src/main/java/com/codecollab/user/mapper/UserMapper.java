package com.codecollab.user.mapper;

import com.codecollab.user.dto.RegisterRequest;
import com.codecollab.user.dto.UserResponse;
import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }

    public User toEntity(RegisterRequest req, String passwordHash) {
        return User.builder()
            .username(req.username())
            .email(req.email().toLowerCase())
            .passwordHash(passwordHash)
            .role(Role.USER)
            .status(UserStatus.ACTIVE)
            .build();
    }
}
