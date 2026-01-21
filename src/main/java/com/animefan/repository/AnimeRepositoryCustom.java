package com.animefan.repository;

import com.animefan.dto.AnimeSearchDTO;
import com.animefan.dto.GenreStatsDTO;
import com.animefan.model.Anime;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Custom repository interface for complex Anime queries
 */
public interface AnimeRepositoryCustom {

    /**
     * Advanced search with filters, pagination and sorting
     */
    Page<Anime> searchAnime(AnimeSearchDTO searchDTO);

    /**
     * Full-text search by title and description
     */
    Page<Anime> fullTextSearch(String query, int page, int size);

    /**
     * Get genre statistics with aggregation pipeline
     */
    List<GenreStatsDTO> getGenreStats();

    /**
     * Get top anime by rating with aggregation
     */
    List<Anime> getTopAnimeByRating(int limit);

    /**
     * Update anime rating based on reviews
     */
    void updateAnimeRating(String animeId, double newRating, int ratingCount);

    /**
     * Increment view count
     */
    void incrementViewCount(String animeId);

    /**
     * Increment/decrement favorite count
     */
    void updateFavoriteCount(String animeId, int delta);

    /**
     * Add episode to anime
     */
    void addEpisode(String animeId, Anime.Episode episode);

    /**
     * Update specific episode
     */
    void updateEpisode(String animeId, int episodeNumber, Anime.Episode episode);

    /**
     * Remove episode from anime
     */
    void removeEpisode(String animeId, int episodeNumber);
}
