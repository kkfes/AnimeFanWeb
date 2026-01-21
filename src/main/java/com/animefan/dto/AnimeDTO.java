package com.animefan.dto;

import com.animefan.model.Anime;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Anime entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimeDTO {

    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    private String titleEnglish;
    private String titleJapanese;

    @NotEmpty(message = "At least one genre is required")
    private List<String> genres;

    @Min(value = 1900, message = "Release year must be after 1900")
    @Max(value = 2100, message = "Release year must be before 2100")
    private Integer releaseYear;

    private LocalDate releaseDate;

    private String status;
    private String type;
    private Integer episodeCount;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private String studioId;
    private String studioName;

    private Double rating;
    private Integer ratingCount;
    private Integer viewCount;
    private Integer favoriteCount;

    private List<Anime.Episode> episodes;

    private List<Anime.RelatedAnime> relatedAnime;

    /**
     * Convert entity to DTO
     */
    public static AnimeDTO fromEntity(Anime anime) {
        if (anime == null) return null;
        return AnimeDTO.builder()
                .id(anime.getId())
                .title(anime.getTitle())
                .description(anime.getDescription())
                .titleEnglish(anime.getTitleEnglish())
                .titleJapanese(anime.getTitleJapanese())
                .genres(anime.getGenres())
                .releaseYear(anime.getReleaseYear())
                .releaseDate(anime.getReleaseDate())
                .status(anime.getStatus())
                .type(anime.getType())
                .episodeCount(anime.getEpisodeCount())
                .posterUrl(anime.getPosterUrl())
                .bannerUrl(anime.getBannerUrl())
                .trailerUrl(anime.getTrailerUrl())
                .studioId(anime.getStudioId())
                .studioName(anime.getStudioName())
                .rating(anime.getRating())
                .ratingCount(anime.getRatingCount())
                .viewCount(anime.getViewCount())
                .favoriteCount(anime.getFavoriteCount())
                .episodes(anime.getEpisodes())
                .relatedAnime(anime.getRelatedAnime())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public Anime toEntity() {
        return Anime.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .titleEnglish(this.titleEnglish)
                .titleJapanese(this.titleJapanese)
                .genres(this.genres)
                .releaseYear(this.releaseYear)
                .releaseDate(this.releaseDate)
                .status(this.status)
                .type(this.type)
                .episodeCount(this.episodeCount)
                .posterUrl(this.posterUrl)
                .bannerUrl(this.bannerUrl)
                .trailerUrl(this.trailerUrl)
                .studioId(this.studioId)
                .studioName(this.studioName)
                .rating(this.rating != null ? this.rating : 0.0)
                .ratingCount(this.ratingCount != null ? this.ratingCount : 0)
                .viewCount(this.viewCount != null ? this.viewCount : 0)
                .favoriteCount(this.favoriteCount != null ? this.favoriteCount : 0)
                .episodes(this.episodes)
                .relatedAnime(this.relatedAnime)
                .build();
    }
}
