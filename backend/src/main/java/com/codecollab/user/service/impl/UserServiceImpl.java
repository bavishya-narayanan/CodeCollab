package com.codecollab.user.service.impl;

import com.codecollab.security.JwtService;
import com.codecollab.user.dto.AuthResponse;
import com.codecollab.user.dto.ChangePasswordRequest;
import com.codecollab.user.dto.LoginRequest;
import com.codecollab.user.dto.RegisterRequest;
import com.codecollab.user.dto.UpdateProfileRequest;
import com.codecollab.user.dto.UserResponse;
import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.mapper.UserMapper;
import com.codecollab.user.repository.UserRepository;
import com.codecollab.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashed = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, hashed);
        user = userRepository.save(user);

        log.info("user registered: {}", user.getUsername());
        return userMapper.toResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
            .findByUsernameIgnoreCaseOrEmailIgnoreCase(request.usernameOrEmail(), request.usernameOrEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new DisabledException("Account banned");
        }
        if (user.getStatus() == UserStatus.PENDING) {
            throw new DisabledException("Account not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(), userMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.username())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new IllegalArgumentException("Email is already registered");
            }
            user.setEmail(request.email().toLowerCase());
        }

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(UserStatus status, Pageable pageable) {
        Page<User> users = (status != null)
            ? userRepository.findByStatus(status, pageable)
            : userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse banUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setStatus(UserStatus.BANNED);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse unbanUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setRole(Role.ADMIN);
        return userMapper.toResponse(userRepository.save(user));
    }
}
