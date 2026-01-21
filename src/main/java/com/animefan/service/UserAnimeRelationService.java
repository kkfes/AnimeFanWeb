package com.animefan.service;

import com.animefan.dto.UserAnimeRelationDTO;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.model.Anime;
import com.animefan.model.UserAnimeRelation;
import com.animefan.repository.AnimeRepository;
import com.animefan.repository.UserAnimeRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for UserAnimeRelation business logic (user anime lists)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnimeRelationService {

    private final UserAnimeRelationRepository relationRepository;
    private final AnimeRepository animeRepository;
    private final UserService userService;

    /**
     * Get all relations for user with pagination
     */
    public Page<UserAnimeRelation> getUserAnimeList(String userId, int page, int size) {
        log.info("Getting anime list for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return relationRepository.findByUserId(userId, pageable);
    }

    /**
     * Get user's anime by status
     */
    public Page<UserAnimeRelation> getUserAnimeByStatus(String userId, UserAnimeRelation.Status status,
                                                         int page, int size) {
        log.info("Getting {} list for user: {}", status, userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return relationRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    /**
     * Get user's favorites
     */
    public Page<UserAnimeRelation> getUserFavorites(String userId, int page, int size) {
        log.info("Getting favorites for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "addedAt"));
        return relationRepository.findByUserIdAndFavoriteTrue(userId, pageable);
    }

    /**
     * Get relation by ID
     */
    public UserAnimeRelation getRelationById(String id) {
        return relationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserAnimeRelation", "id", id));
    }

    /**
     * Get relation by user and anime
     */
    public Optional<UserAnimeRelation> getRelation(String userId, String animeId) {
        return relationRepository.findByUserIdAndAnimeId(userId, animeId);
    }

    /**
     * Add anime to user's list
     */
    public UserAnimeRelation addAnimeToList(String userId, UserAnimeRelationDTO dto) {
        log.info("Adding anime {} to user {} list with status {}", dto.getAnimeId(), userId, dto.getStatus());

        // Check if relation already exists
        Optional<UserAnimeRelation> existing = relationRepository.findByUserIdAndAnimeId(userId, dto.getAnimeId());
        if (existing.isPresent()) {
            return updateRelation(existing.get().getId(), userId, dto);
        }

        // Get anime details
        Anime anime = animeRepository.findById(dto.getAnimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Anime", "id", dto.getAnimeId()));

        UserAnimeRelation relation = UserAnimeRelation.builder()
                .userId(userId)
                .animeId(anime.getId())
                .animeTitle(anime.getTitle())
                .animePosterUrl(anime.getPosterUrl())
                .animeRating(anime.getRating())
                .status(dto.getStatus())
                .userRating(dto.getUserRating())
                .episodesWatched(dto.getEpisodesWatched() != null ? dto.getEpisodesWatched() : 0)
                .totalEpisodes(anime.getEpisodeCount())
                .notes(dto.getNotes())
                .favorite(dto.getFavorite() != null ? dto.getFavorite() : false)
                .startedAt(dto.getStatus() == UserAnimeRelation.Status.WATCHING ? LocalDateTime.now() : null)
                .completedAt(dto.getStatus() == UserAnimeRelation.Status.COMPLETED ? LocalDateTime.now() : null)
                .build();

        UserAnimeRelation saved = relationRepository.save(relation);

        // Update favorite count if needed
        if (saved.getFavorite()) {
            animeRepository.updateFavoriteCount(anime.getId(), 1);
            userService.incrementFavoriteCount(userId, 1);
        }

        // Update watched count if completed
        if (saved.getStatus() == UserAnimeRelation.Status.COMPLETED) {
            userService.incrementWatchedCount(userId);
        }

        return saved;
    }

    /**
     * Update relation
     */
    public UserAnimeRelation updateRelation(String relationId, String userId, UserAnimeRelationDTO dto) {
        log.info("Updating relation: {}", relationId);

        UserAnimeRelation relation = getRelationById(relationId);

        // Verify ownership
        if (!relation.getUserId().equals(userId)) {
            throw new SecurityException("You can only modify your own lists");
        }

        boolean wasCompleted = relation.getStatus() == UserAnimeRelation.Status.COMPLETED;
        boolean wasFavorite = Boolean.TRUE.equals(relation.getFavorite());

        // Update fields
        if (dto.getStatus() != null) {
            relation.setStatus(dto.getStatus());

            if (dto.getStatus() == UserAnimeRelation.Status.WATCHING && relation.getStartedAt() == null) {
                relation.setStartedAt(LocalDateTime.now());
            }
            if (dto.getStatus() == UserAnimeRelation.Status.COMPLETED && relation.getCompletedAt() == null) {
                relation.setCompletedAt(LocalDateTime.now());
            }
        }

        if (dto.getUserRating() != null) {
            relation.setUserRating(dto.getUserRating());
        }
        if (dto.getEpisodesWatched() != null) {
            relation.setEpisodesWatched(dto.getEpisodesWatched());
        }
        if (dto.getNotes() != null) {
            relation.setNotes(dto.getNotes());
        }
        if (dto.getFavorite() != null) {
            relation.setFavorite(dto.getFavorite());
        }

        UserAnimeRelation updated = relationRepository.save(relation);

        // Handle favorite changes
        boolean nowFavorite = Boolean.TRUE.equals(updated.getFavorite());
        if (wasFavorite != nowFavorite) {
            int delta = nowFavorite ? 1 : -1;
            animeRepository.updateFavoriteCount(relation.getAnimeId(), delta);
            userService.incrementFavoriteCount(userId, delta);
        }

        // Handle completion changes
        boolean nowCompleted = updated.getStatus() == UserAnimeRelation.Status.COMPLETED;
        if (!wasCompleted && nowCompleted) {
            userService.incrementWatchedCount(userId);
        }

        return updated;
    }

    /**
     * Update watch progress
     */
    public void updateProgress(String relationId, String userId, int episodesWatched) {
        log.info("Updating progress for relation: {}", relationId);

        UserAnimeRelation relation = getRelationById(relationId);

        if (!relation.getUserId().equals(userId)) {
            throw new SecurityException("You can only modify your own lists");
        }

        relation.setEpisodesWatched(episodesWatched);

        // Auto-complete if all episodes watched
        if (relation.getTotalEpisodes() != null && episodesWatched >= relation.getTotalEpisodes()) {
            relation.setStatus(UserAnimeRelation.Status.COMPLETED);
            relation.setCompletedAt(LocalDateTime.now());
            userService.incrementWatchedCount(userId);
        }

        relationRepository.save(relation);
    }

    /**
     * Toggle favorite
     */
    public void toggleFavorite(String userId, String animeId) {
        log.info("Toggling favorite for user {} and anime {}", userId, animeId);

        Optional<UserAnimeRelation> existing = relationRepository.findByUserIdAndAnimeId(userId, animeId);

        if (existing.isPresent()) {
            UserAnimeRelation relation = existing.get();
            boolean newFavorite = !Boolean.TRUE.equals(relation.getFavorite());
            relation.setFavorite(newFavorite);
            relationRepository.save(relation);

            int delta = newFavorite ? 1 : -1;
            animeRepository.updateFavoriteCount(animeId, delta);
            userService.incrementFavoriteCount(userId, delta);
        } else {
            // Create new relation with favorite
            Anime anime = animeRepository.findById(animeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Anime", "id", animeId));

            UserAnimeRelation relation = UserAnimeRelation.builder()
                    .userId(userId)
                    .animeId(animeId)
                    .animeTitle(anime.getTitle())
                    .animePosterUrl(anime.getPosterUrl())
                    .animeRating(anime.getRating())
                    .status(UserAnimeRelation.Status.PLAN_TO_WATCH)
                    .favorite(true)
                    .episodesWatched(0)
                    .totalEpisodes(anime.getEpisodeCount())
                    .build();

            relationRepository.save(relation);
            animeRepository.updateFavoriteCount(animeId, 1);
            userService.incrementFavoriteCount(userId, 1);
        }
    }

    /**
     * Remove anime from user's list
     */
    public void removeFromList(String relationId, String userId) {
        log.info("Removing anime from user's list: {}", relationId);

        UserAnimeRelation relation = getRelationById(relationId);

        if (!relation.getUserId().equals(userId)) {
            throw new SecurityException("You can only modify your own lists");
        }

        // Update counters
        if (Boolean.TRUE.equals(relation.getFavorite())) {
            animeRepository.updateFavoriteCount(relation.getAnimeId(), -1);
            userService.incrementFavoriteCount(userId, -1);
        }

        relationRepository.deleteById(relationId);
    }

    /**
     * Remove anime from user's list by anime ID
     */
    public void removeAnimeFromUserList(String userId, String animeId) {
        log.info("Removing anime {} from user {} list", animeId, userId);

        Optional<UserAnimeRelation> relationOpt = relationRepository.findByUserIdAndAnimeId(userId, animeId);

        if (relationOpt.isPresent()) {
            UserAnimeRelation relation = relationOpt.get();

            // Update counters
            if (Boolean.TRUE.equals(relation.getFavorite())) {
                animeRepository.updateFavoriteCount(animeId, -1);
                userService.incrementFavoriteCount(userId, -1);
            }

            relationRepository.delete(relation);
        }
    }

    /**
     * Get user statistics by status
     */
    public List<UserAnimeRelationRepository.StatusCount> getUserStatusCounts(String userId) {
        return relationRepository.getUserStatusCounts(userId);
    }

    /**
     * Get user's genre preferences
     */
    public List<UserAnimeRelationRepository.GenrePreference> getUserGenrePreferences(String userId) {
        return relationRepository.getUserGenrePreferences(userId);
    }

    /**
     * Check if anime is in user's list
     */
    public boolean isInUserList(String userId, String animeId) {
        return relationRepository.existsByUserIdAndAnimeId(userId, animeId);
    }

    /**
     * Check if anime is in user's favorites
     */
    public boolean isInFavorites(String userId, String animeId) {
        return relationRepository.findByUserIdAndAnimeId(userId, animeId)
                .map(r -> Boolean.TRUE.equals(r.getFavorite()))
                .orElse(false);
    }

    /**
     * Get count by status for user
     */
    public long getCountByStatus(String userId, UserAnimeRelation.Status status) {
        return relationRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * Get favorites count for user
     */
    public long getFavoritesCount(String userId) {
        return relationRepository.countByUserIdAndFavoriteTrue(userId);
    }
}
