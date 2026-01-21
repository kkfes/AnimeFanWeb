package com.animefan.dto;

import com.animefan.model.UserAnimeRelation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for UserAnimeRelation entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnimeRelationDTO {

    private String id;

    private String userId;

    private String animeId;

    private String animeTitle;
    private String animePosterUrl;
    private Double animeRating;

    private UserAnimeRelation.Status status;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Integer userRating;

    @Min(value = 0, message = "Episodes watched cannot be negative")
    private Integer episodesWatched;
    private Integer totalEpisodes;

    private String notes;
    private Boolean favorite;

    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /**
     * Convert entity to DTO
     */
    public static UserAnimeRelationDTO fromEntity(UserAnimeRelation relation) {
        if (relation == null) return null;
        return UserAnimeRelationDTO.builder()
                .id(relation.getId())
                .userId(relation.getUserId())
                .animeId(relation.getAnimeId())
                .animeTitle(relation.getAnimeTitle())
                .animePosterUrl(relation.getAnimePosterUrl())
                .animeRating(relation.getAnimeRating())
                .status(relation.getStatus())
                .userRating(relation.getUserRating())
                .episodesWatched(relation.getEpisodesWatched())
                .totalEpisodes(relation.getTotalEpisodes())
                .notes(relation.getNotes())
                .favorite(relation.getFavorite())
                .addedAt(relation.getAddedAt())
                .updatedAt(relation.getUpdatedAt())
                .startedAt(relation.getStartedAt())
                .completedAt(relation.getCompletedAt())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public UserAnimeRelation toEntity() {
        return UserAnimeRelation.builder()
                .id(this.id)
                .userId(this.userId)
                .animeId(this.animeId)
                .animeTitle(this.animeTitle)
                .animePosterUrl(this.animePosterUrl)
                .animeRating(this.animeRating)
                .status(this.status)
                .userRating(this.userRating)
                .episodesWatched(this.episodesWatched != null ? this.episodesWatched : 0)
                .totalEpisodes(this.totalEpisodes)
                .notes(this.notes)
                .favorite(this.favorite != null ? this.favorite : false)
                .build();
    }
}
