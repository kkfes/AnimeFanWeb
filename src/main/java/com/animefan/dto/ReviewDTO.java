package com.animefan.dto;

import com.animefan.model.Review;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Review entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private String id;

    private String userId;
    private String username;
    private String userAvatarUrl;

    @NotBlank(message = "Anime ID is required")
    private String animeId;
    private String animeTitle;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Integer rating;

    private String title;

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 5000, message = "Review must be between 10 and 5000 characters")
    private String text;

    private Boolean spoiler = false;

    private Integer helpfulCount;
    private Integer unhelpfulCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert entity to DTO
     */
    public static ReviewDTO fromEntity(Review review) {
        if (review == null) return null;
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .username(review.getUsername())
                .userAvatarUrl(review.getUserAvatarUrl())
                .animeId(review.getAnimeId())
                .animeTitle(review.getAnimeTitle())
                .rating(review.getRating())
                .title(review.getTitle())
                .text(review.getText())
                .spoiler(review.getSpoiler())
                .helpfulCount(review.getHelpfulCount())
                .unhelpfulCount(review.getUnhelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public Review toEntity() {
        return Review.builder()
                .id(this.id)
                .userId(this.userId)
                .username(this.username)
                .userAvatarUrl(this.userAvatarUrl)
                .animeId(this.animeId)
                .animeTitle(this.animeTitle)
                .rating(this.rating)
                .title(this.title)
                .text(this.text)
                .spoiler(this.spoiler != null ? this.spoiler : false)
                .helpfulCount(this.helpfulCount != null ? this.helpfulCount : 0)
                .unhelpfulCount(this.unhelpfulCount != null ? this.unhelpfulCount : 0)
                .build();
    }
}
