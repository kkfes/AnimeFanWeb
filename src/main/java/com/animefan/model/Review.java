package com.animefan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Review entity - separate collection for anime reviews
 * Uses referenced documents pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
@CompoundIndex(name = "user_anime_idx", def = "{'userId': 1, 'animeId': 1}", unique = true)
@CompoundIndex(name = "anime_rating_idx", def = "{'animeId': 1, 'rating': -1}")
public class Review {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String username;

    private String userAvatarUrl;

    @Indexed
    private String animeId;

    private String animeTitle;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Integer rating;

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 5000, message = "Review must be between 10 and 5000 characters")
    private String text;

    private String title;

    // Review reactions
    private Integer helpfulCount = 0;
    private Integer unhelpfulCount = 0;

    private Boolean spoiler = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
