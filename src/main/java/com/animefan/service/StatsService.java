package com.animefan.service;

import com.animefan.dto.GenreStatsDTO;
import com.animefan.model.Anime;
import com.animefan.model.UserAnimeRelation;
import com.animefan.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for platform statistics and analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AnimeRepository animeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final StudioRepository studioRepository;
    private final UserAnimeRelationRepository relationRepository;

    /**
     * Get platform-wide statistics
     */
    @Cacheable(value = "platformStats", key = "'stats'")
    public PlatformStats getPlatformStats() {
        log.info("Calculating platform statistics");

        long totalAnime = animeRepository.count();
        long totalUsers = userRepository.count();
        long totalReviews = reviewRepository.count();
        long totalStudios = studioRepository.count();

        List<GenreStatsDTO> genreStats = animeRepository.getGenreStats();
        List<Anime> topAnime = animeRepository.getTopAnimeByRating(10);

        return PlatformStats.builder()
                .totalAnime(totalAnime)
                .totalUsers(totalUsers)
                .totalReviews(totalReviews)
                .totalStudios(totalStudios)
                .genreStats(genreStats)
                .topAnime(topAnime)
                .build();
    }

    /**
     * Get genre statistics
     */
    @Cacheable(value = "genreStats")
    public List<GenreStatsDTO> getGenreStats() {
        log.info("Getting genre statistics");
        return animeRepository.getGenreStats();
    }

    /**
     * Get top anime by rating
     */
    @Cacheable(value = "topAnime", key = "#limit")
    public List<Anime> getTopAnimeByRating(int limit) {
        log.info("Getting top {} anime by rating", limit);
        return animeRepository.getTopAnimeByRating(limit);
    }

    /**
     * Get user statistics
     */
    public UserStats getUserStats(String userId) {
        log.info("Getting statistics for user: {}", userId);

        // Use simple count methods instead of aggregation to avoid projection issues
        long watchingCount = relationRepository.countByUserIdAndStatus(userId, UserAnimeRelation.Status.WATCHING);
        long completedCount = relationRepository.countByUserIdAndStatus(userId, UserAnimeRelation.Status.COMPLETED);
        long onHoldCount = relationRepository.countByUserIdAndStatus(userId, UserAnimeRelation.Status.ON_HOLD);
        long droppedCount = relationRepository.countByUserIdAndStatus(userId, UserAnimeRelation.Status.DROPPED);
        long planToWatchCount = relationRepository.countByUserIdAndStatus(userId, UserAnimeRelation.Status.PLAN_TO_WATCH);

        long favoritesCount = relationRepository.countByUserIdAndFavoriteTrue(userId);
        long reviewCount = reviewRepository.countByUserId(userId);

        return UserStats.builder()
                .userId(userId)
                .watchingCount(watchingCount)
                .completedCount(completedCount)
                .onHoldCount(onHoldCount)
                .droppedCount(droppedCount)
                .planToWatchCount(planToWatchCount)
                .favoritesCount(favoritesCount)
                .reviewCount(reviewCount)
                .totalInList(watchingCount + completedCount + onHoldCount + droppedCount + planToWatchCount)
                .build();
    }

    /**
     * Get anime statistics
     */
    public AnimeStats getAnimeStats(String animeId) {
        log.info("Getting statistics for anime: {}", animeId);

        Anime anime = animeRepository.findById(animeId).orElse(null);
        if (anime == null) {
            return null;
        }

        long userCount = relationRepository.countByAnimeId(animeId);
        long favoriteCount = relationRepository.countByAnimeIdAndFavoriteTrue(animeId);
        long reviewCount = reviewRepository.countByAnimeId(animeId);

        return AnimeStats.builder()
                .animeId(animeId)
                .title(anime.getTitle())
                .rating(anime.getRating())
                .ratingCount(anime.getRatingCount())
                .viewCount(anime.getViewCount())
                .userCount(userCount)
                .favoriteCount(favoriteCount)
                .reviewCount(reviewCount)
                .build();
    }

    /**
     * Get top reviewers
     */
    public List<ReviewRepository.TopReviewer> getTopReviewers(int limit) {
        log.info("Getting top {} reviewers", limit);
        try {
            return reviewRepository.getTopReviewers(limit);
        } catch (Exception e) {
            log.warn("Failed to get top reviewers: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    @Data
    @Builder
    public static class PlatformStats {
        private long totalAnime;
        private long totalUsers;
        private long totalReviews;
        private long totalStudios;
        private List<GenreStatsDTO> genreStats;
        private List<Anime> topAnime;
    }

    @Data
    @Builder
    public static class UserStats {
        private String userId;
        private long watchingCount;
        private long completedCount;
        private long onHoldCount;
        private long droppedCount;
        private long planToWatchCount;
        private long favoritesCount;
        private long reviewCount;
        private long totalInList;
    }

    @Data
    @Builder
    public static class AnimeStats {
        private String animeId;
        private String title;
        private Double rating;
        private Integer ratingCount;
        private Integer viewCount;
        private long userCount;
        private long favoriteCount;
        private long reviewCount;
    }
}
