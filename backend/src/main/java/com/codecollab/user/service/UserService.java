package com.codecollab.user.service;

import com.codecollab.user.dto.AuthResponse;
import com.codecollab.user.dto.ChangePasswordRequest;
import com.codecollab.user.dto.LoginRequest;
import com.codecollab.user.dto.RegisterRequest;
import com.codecollab.user.dto.UpdateProfileRequest;
import com.codecollab.user.dto.UserResponse;
import com.codecollab.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    Page<UserResponse> listUsers(UserStatus status, Pageable pageable);

    UserResponse banUser(Long userId);

    UserResponse unbanUser(Long userId);

    UserResponse promoteToAdmin(Long userId);
}
