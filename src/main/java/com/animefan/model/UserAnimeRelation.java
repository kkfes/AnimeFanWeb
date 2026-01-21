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

import java.time.LocalDateTime;

/**
 * UserAnimeRelation entity - tracks user's anime lists
 * Supports FAVORITE, WATCHING, COMPLETED, ON_HOLD, DROPPED, PLAN_TO_WATCH statuses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_anime_relations")
@CompoundIndex(name = "user_anime_unique_idx", def = "{'userId': 1, 'animeId': 1}", unique = true)
@CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}")
public class UserAnimeRelation {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String animeId;

    // Denormalized data for faster queries
    private String animeTitle;
    private String animePosterUrl;
    private Double animeRating;

    private Status status;

    // User's personal rating (separate from review rating)
    private Integer userRating;

    // Progress tracking
    private Integer episodesWatched = 0;
    private Integer totalEpisodes;

    // User notes
    private String notes;

    private Boolean favorite = false;

    @CreatedDate
    private LocalDateTime addedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public enum Status {
        WATCHING("Смотрю"),
        COMPLETED("Просмотрено"),
        ON_HOLD("Отложено"),
        DROPPED("Брошено"),
        PLAN_TO_WATCH("Запланировано");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
