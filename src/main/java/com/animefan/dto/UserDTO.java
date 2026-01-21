package com.animefan.dto;

import com.animefan.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for User entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String displayName;
    private String avatarUrl;
    private String bio;
    private String role;

    private Integer watchedCount;
    private Integer reviewCount;
    private Integer favoriteCount;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /**
     * Convert entity to DTO (excludes password)
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .watchedCount(user.getWatchedCount())
                .reviewCount(user.getReviewCount())
                .favoriteCount(user.getFavoriteCount())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
