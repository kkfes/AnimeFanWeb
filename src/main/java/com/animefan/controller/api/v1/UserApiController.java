package com.animefan.controller.api.v1;

import com.animefan.dto.UserDTO;
import com.animefan.model.User;
import com.animefan.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for User operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserApiController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of users (Admin only)")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/users - page: {}, size: {}", page, size);
        Page<User> users = userService.getAllUsers(page, size);
        Page<UserDTO> userDTOs = users.map(UserDTO::fromEntity);
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        log.info("GET /api/v1/users/{}", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Get user details by username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/v1/users/username/{}", username);
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("GET /api/v1/users/me");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register new user account")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/users/register - username: {}", request.getUsername());
        User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update current user's profile")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserDTO userDTO) {

        log.info("PUT /api/v1/users/me");
        User updated = userService.updateUserProfile(currentUser.getId(), userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @PutMapping("/me/email")
    @Operation(summary = "Update email", description = "Update current user's email")
    public ResponseEntity<UserDTO> updateEmail(
            @AuthenticationPrincipal User currentUser,
            @RequestBody EmailUpdateRequest request) {

        log.info("PUT /api/v1/users/me/email");
        User updated = userService.updateEmail(currentUser.getId(), request.getEmail());
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password", description = "Change current user's password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {

        log.info("PUT /api/v1/users/me/password");
        userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Update user's role (Admin only)")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable String id,
            @RequestBody RoleUpdateRequest request) {

        log.info("PUT /api/v1/users/{}/role - role: {}", id, request.getRole());
        User updated = userService.updateUserRole(id, User.Role.valueOf(request.getRole()));
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable/disable user", description = "Enable or disable user account (Admin only)")
    public ResponseEntity<UserDTO> setUserEnabled(
            @PathVariable String id,
            @RequestParam boolean enabled) {

        log.info("PUT /api/v1/users/{}/enabled - enabled: {}", id, enabled);
        User updated = userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user account (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("DELETE /api/v1/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by username")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/users/search - query: {}", q);
        Page<User> users = userService.searchUsers(q, page, size);
        Page<UserDTO> userDTOs = users.map(UserDTO::fromEntity);
        return ResponseEntity.ok(userDTOs);
    }

    // Request DTOs

    @lombok.Data
    public static class RegisterRequest {
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 3, max = 50)
        private String username;

        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        private String email;

        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 6)
        private String password;

        private String displayName;
    }

    @lombok.Data
    public static class EmailUpdateRequest {
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        private String email;
    }

    @lombok.Data
    public static class PasswordChangeRequest {
        @jakarta.validation.constraints.NotBlank
        private String oldPassword;

        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 6)
        private String newPassword;
    }

    @lombok.Data
    public static class RoleUpdateRequest {
        @jakarta.validation.constraints.NotBlank
        private String role;
    }
}
