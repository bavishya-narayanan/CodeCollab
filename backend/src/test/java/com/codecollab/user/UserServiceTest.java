package com.codecollab.user;

import com.codecollab.security.JwtService;
import com.codecollab.user.dto.AuthResponse;
import com.codecollab.user.dto.ChangePasswordRequest;
import com.codecollab.user.dto.LoginRequest;
import com.codecollab.user.dto.RegisterRequest;
import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.mapper.UserMapper;
import com.codecollab.user.repository.UserRepository;
import com.codecollab.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_throwsIfUsernameExists() {
        when(userRepository.existsByUsernameIgnoreCase("test")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("test", "test@example.com", "Password123!");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void register_throwsIfEmailExists() {
        when(userRepository.existsByUsernameIgnoreCase("test")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("test", "test@example.com", "Password123!");
        assertThrows(IllegalArgumentException.class, () -> userService.register(req));
    }

    @Test
    void register_hashesPasswordAndSaves() {
        when(userRepository.existsByUsernameIgnoreCase("test")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        
        User savedUser = new User();
        savedUser.setUsername("test");
        savedUser.setEmail("test@example.com");
        savedUser.setPasswordHash("hashed_pass");
        savedUser.setRole(Role.USER);
        savedUser.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterRequest req = new RegisterRequest("test", "test@example.com", "Password123!");
        userService.register(req);

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_throwsBadCredentialsIfWrongPassword() {
        User user = new User();
        user.setPasswordHash("hashed_pass");
        user.setStatus(UserStatus.ACTIVE);
        
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("test", "test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed_pass")).thenReturn(false);

        LoginRequest req = new LoginRequest("test", "wrong");
        assertThrows(BadCredentialsException.class, () -> userService.login(req));
    }

    @Test
    void login_throwsDisabledExceptionIfBanned() {
        User user = new User();
        user.setStatus(UserStatus.BANNED);
        
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("test", "test")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest("test", "pass");
        assertThrows(DisabledException.class, () -> userService.login(req));
    }

    @Test
    void login_updatesLastLoginAndReturnsToken() {
        User user = new User();
        user.setPasswordHash("hashed_pass");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);
        
        when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("test", "test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed_pass")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt_token");

        LoginRequest req = new LoginRequest("test", "pass");
        AuthResponse resp = userService.login(req);

        verify(userRepository).save(user);
        verify(jwtService).generateToken(user);
    }

    @Test
    void changePassword_throwsIfCurrentPasswordWrong() {
        User user = new User();
        user.setPasswordHash("hashed_pass");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed_pass")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest("wrong", "NewPass123!");
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, req));
    }
}
