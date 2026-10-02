package com.codecollab.user.controller;

import com.codecollab.common.ApiResponse;
import com.codecollab.user.dto.UserResponse;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Users")
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> list(
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
            userService.listUsers(status, pageable)));
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> ban(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User banned",
            userService.banUser(id)));
    }

    @PostMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unban(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User unbanned",
            userService.unbanUser(id)));
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<ApiResponse<UserResponse>> promote(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User promoted to ADMIN",
            userService.promoteToAdmin(id)));
    }
}
