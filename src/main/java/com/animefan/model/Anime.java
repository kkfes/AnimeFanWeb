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
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Anime entity with embedded episodes
 * Uses MongoDB text index for full-text search on title and description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "anime")
@CompoundIndex(name = "genre_rating_idx", def = "{'genres': 1, 'rating': -1}")
@CompoundIndex(name = "studio_year_idx", def = "{'studioId': 1, 'releaseYear': -1}")
public class Anime {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @TextIndexed(weight = 3)
    @Indexed
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    @TextIndexed(weight = 2)
    private String description;

    @TextIndexed
    private String titleEnglish;

    @TextIndexed
    private String titleJapanese;

    @Indexed
    private List<String> genres = new ArrayList<>();

    @Min(value = 0, message = "Rating cannot be negative")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Double rating = 0.0;

    private Integer ratingCount = 0;

    @Indexed
    private Integer releaseYear;

    private LocalDate releaseDate;

    private String status; // ONGOING, COMPLETED, UPCOMING

    private String type; // TV, MOVIE, OVA, ONA, SPECIAL

    private Integer episodeCount;

    private String posterUrl;

    private String bannerUrl;

    private String trailerUrl;

    @Indexed
    private String studioId;

    private String studioName;

    // Embedded episodes
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();

    // Related anime (seasons, sequels, prequels, etc.)
    @Builder.Default
    private List<RelatedAnime> relatedAnime = new ArrayList<>();

    private Integer viewCount = 0;

    private Integer favoriteCount = 0;

    @TextScore
    private Float score;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Embedded Episode document
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Episode {
        private Integer number;
        private String title;
        private String description;
        private Integer duration; // in minutes
        private String videoUrl;
        private String thumbnailUrl;
        private LocalDate airDate;
        private Boolean filler = false;
    }

    /**
     * Embedded Related Anime document
     * Stores reference to another anime with relation type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedAnime {
        private String animeId;       // ID of the related anime
        private String title;         // Title for display (cached)
        private String posterUrl;     // Poster URL (cached)
        private RelationType relationType;
        private Integer seasonNumber; // For SEASON type, indicates season number

        public enum RelationType {
            SEQUEL("Продолжение"),
            PREQUEL("Приквел"),
            SEASON("Сезон"),
            SIDE_STORY("Побочная история"),
            SPIN_OFF("Спин-офф"),
            ALTERNATIVE("Альтернативная версия"),
            MOVIE("Фильм"),
            OVA("OVA"),
            SPECIAL("Спецвыпуск"),
            OTHER("Связанное");

            private final String displayName;

            RelationType(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }
    }
}
