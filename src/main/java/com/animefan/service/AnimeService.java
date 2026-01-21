package com.animefan.service;

import com.animefan.dto.AnimeDTO;
import com.animefan.dto.AnimeSearchDTO;
import com.animefan.dto.GenreStatsDTO;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.model.Anime;
import com.animefan.model.Review;
import com.animefan.repository.AnimeRepository;
import com.animefan.repository.ReviewRepository;
import com.animefan.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Anime business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final StudioRepository studioRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Get all anime with pagination
     */
    public Page<Anime> getAllAnime(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all anime, page: {}, size: {}", page, size);
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        return animeRepository.findAll(pageable);
    }

    /**
     * Get anime by ID
     */
    public Anime getAnimeById(String id) {
        log.info("Getting anime by ID: {}", id);
        return animeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", "id", id));
    }

    /**
     * Get anime by ID and increment view count
     */
    @Transactional
    public Anime getAnimeByIdWithView(String id) {
        Anime anime = getAnimeById(id);
        animeRepository.incrementViewCount(id);
        anime.setViewCount(anime.getViewCount() + 1);
        return anime;
    }

    /**
     * Create new anime
     */
    @Transactional
    @CacheEvict(value = {"topAnime", "genreStats"}, allEntries = true)
    public Anime createAnime(AnimeDTO animeDTO) {
        log.info("Creating new anime: {}", animeDTO.getTitle());

        if (animeRepository.existsByTitle(animeDTO.getTitle())) {
            throw new ValidationException("Anime with this title already exists");
        }

        Anime anime = animeDTO.toEntity();

        // Set studio name if studio ID is provided
        if (anime.getStudioId() != null) {
            studioRepository.findById(anime.getStudioId())
                    .ifPresent(studio -> {
                        anime.setStudioName(studio.getName());
                        studioRepository.addAnimeToStudio(studio.getId(), anime.getId());
                    });
        }

        return animeRepository.save(anime);
    }

    /**
     * Update anime
     */
    @Transactional
    @CacheEvict(value = {"topAnime", "genreStats"}, allEntries = true)
    public Anime updateAnime(String id, AnimeDTO animeDTO) {
        log.info("Updating anime: {}", id);

        Anime existingAnime = getAnimeById(id);

        // Update fields
        existingAnime.setTitle(animeDTO.getTitle());
        existingAnime.setDescription(animeDTO.getDescription());
        existingAnime.setTitleEnglish(animeDTO.getTitleEnglish());
        existingAnime.setTitleJapanese(animeDTO.getTitleJapanese());
        existingAnime.setGenres(animeDTO.getGenres());
        existingAnime.setReleaseYear(animeDTO.getReleaseYear());
        existingAnime.setReleaseDate(animeDTO.getReleaseDate());
        existingAnime.setStatus(animeDTO.getStatus());
        existingAnime.setType(animeDTO.getType());
        existingAnime.setEpisodeCount(animeDTO.getEpisodeCount());
        existingAnime.setPosterUrl(animeDTO.getPosterUrl());
        existingAnime.setBannerUrl(animeDTO.getBannerUrl());
        existingAnime.setTrailerUrl(animeDTO.getTrailerUrl());

        // Update studio if changed
        if (animeDTO.getStudioId() != null &&
            !animeDTO.getStudioId().equals(existingAnime.getStudioId())) {

            // Remove from old studio
            if (existingAnime.getStudioId() != null) {
                studioRepository.removeAnimeFromStudio(existingAnime.getStudioId(), id);
            }

            // Add to new studio
            studioRepository.findById(animeDTO.getStudioId())
                    .ifPresent(studio -> {
                        existingAnime.setStudioId(studio.getId());
                        existingAnime.setStudioName(studio.getName());
                        studioRepository.addAnimeToStudio(studio.getId(), id);
                    });
        }

        return animeRepository.save(existingAnime);
    }

    /**
     * Delete anime
     */
    @Transactional
    @CacheEvict(value = {"topAnime", "genreStats"}, allEntries = true)
    public void deleteAnime(String id) {
        log.info("Deleting anime: {}", id);

        Anime anime = getAnimeById(id);

        // Remove from studio
        if (anime.getStudioId() != null) {
            studioRepository.removeAnimeFromStudio(anime.getStudioId(), id);
        }

        animeRepository.deleteById(id);
    }

    /**
     * Search anime with filters
     */
    public Page<Anime> searchAnime(AnimeSearchDTO searchDTO) {
        log.info("Searching anime with criteria: {}", searchDTO);
        return animeRepository.searchAnime(searchDTO);
    }

    /**
     * Full-text search
     */
    public Page<Anime> fullTextSearch(String query, int page, int size) {
        log.info("Full-text search for: {}", query);
        return animeRepository.fullTextSearch(query, page, size);
    }

    /**
     * Get anime by genre
     */
    public Page<Anime> getAnimeByGenre(String genre, int page, int size) {
        log.info("Getting anime by genre: {}", genre);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
        return animeRepository.findByGenresContaining(genre, pageable);
    }

    /**
     * Get anime by studio
     */
    public Page<Anime> getAnimeByStudio(String studioId, int page, int size) {
        log.info("Getting anime by studio: {}", studioId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseYear"));
        return animeRepository.findByStudioId(studioId, pageable);
    }

    /**
     * Get top anime by rating (cached)
     */
    @Cacheable(value = "topAnime", key = "#limit")
    public List<Anime> getTopAnime(int limit) {
        log.info("Getting top {} anime by rating", limit);
        return animeRepository.getTopAnimeByRating(limit);
    }

    /**
     * Get recently added anime
     */
    public Page<Anime> getRecentAnime(int page, int size) {
        log.info("Getting recent anime");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return animeRepository.findAll(pageable);
    }

    /**
     * Get genre statistics (cached)
     */
    @Cacheable(value = "genreStats")
    public List<GenreStatsDTO> getGenreStatistics() {
        log.info("Getting genre statistics");
        return animeRepository.getGenreStats();
    }

    /**
     * Get all distinct genres
     */
    @Cacheable(value = "genres")
    public List<String> getAllGenres() {
        log.info("Getting all genres");
        return animeRepository.findAllGenres();
    }

    /**
     * Check if anime exists by title
     */
    public boolean existsByTitle(String title) {
        return animeRepository.existsByTitle(title);
    }

    /**
     * Update anime rating (called when review is added/updated/deleted)
     */
    @CacheEvict(value = {"topAnime", "genreStats"}, allEntries = true)
    public void recalculateRating(String animeId) {
        log.info("Recalculating rating for anime: {}", animeId);

        try {
            List<ReviewRepository.AnimeRatingStats> statsList = reviewRepository.getAnimeRatingStats(animeId);

            if (statsList != null && !statsList.isEmpty()) {
                ReviewRepository.AnimeRatingStats stats = statsList.get(0);
                if (stats.getAvgRating() != null) {
                    double roundedRating = Math.round(stats.getAvgRating() * 10.0) / 10.0;
                    animeRepository.updateAnimeRating(animeId, roundedRating, stats.getCount().intValue());
                    log.info("Updated anime {} rating to {} ({} reviews)", animeId, roundedRating, stats.getCount());
                } else {
                    animeRepository.updateAnimeRating(animeId, 0.0, 0);
                }
            } else {
                animeRepository.updateAnimeRating(animeId, 0.0, 0);
            }
        } catch (Exception e) {
            log.error("Error in aggregation, falling back to simple calculation: {}", e.getMessage());
            // Fallback: calculate manually
            try {
                List<Review> reviews = reviewRepository.findByAnimeId(animeId);
                if (reviews != null && !reviews.isEmpty()) {
                    double avgRating = reviews.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0.0);
                    double roundedRating = Math.round(avgRating * 10.0) / 10.0;
                    animeRepository.updateAnimeRating(animeId, roundedRating, reviews.size());
                    log.info("Fallback: Updated anime {} rating to {} ({} reviews)", animeId, roundedRating, reviews.size());
                } else {
                    animeRepository.updateAnimeRating(animeId, 0.0, 0);
                }
            } catch (Exception ex) {
                log.error("Fallback also failed: {}", ex.getMessage());
            }
        }
    }

    /**
     * Add episode to anime
     */
    public void addEpisode(String animeId, Anime.Episode episode) {
        log.info("Adding episode {} to anime: {}", episode.getNumber(), animeId);

        // Validate anime exists
        Anime anime = getAnimeById(animeId);

        // Check if episode already exists
        if (anime.getEpisodes() != null) {
            boolean exists = anime.getEpisodes().stream()
                    .anyMatch(e -> e.getNumber() == episode.getNumber());
            if (exists) {
                throw new IllegalArgumentException("Episode " + episode.getNumber() + " already exists");
            }
        }

        animeRepository.addEpisode(animeId, episode);
    }

    /**
     * Update episode
     */
    public void updateEpisode(String animeId, int episodeNumber, String title, String videoUrl, String thumbnailUrl, int duration) {
        log.info("Updating episode {} in anime: {}", episodeNumber, animeId);

        // Validate anime exists
        Anime anime = getAnimeById(animeId);

        // Find and update episode
        if (anime.getEpisodes() != null) {
            for (Anime.Episode ep : anime.getEpisodes()) {
                if (ep.getNumber() == episodeNumber) {
                    ep.setTitle(title);
                    ep.setVideoUrl(videoUrl);
                    ep.setThumbnailUrl(thumbnailUrl);
                    ep.setDuration(duration);
                    break;
                }
            }
            animeRepository.save(anime);
        }
    }

    /**
     * Delete episode
     */
    public void deleteEpisode(String animeId, int episodeNumber) {
        log.info("Deleting episode {} from anime: {}", episodeNumber, animeId);

        // Validate anime exists
        getAnimeById(animeId);

        animeRepository.removeEpisode(animeId, episodeNumber);
    }

    /**
     * Add related anime
     */
    @Transactional
    public void addRelatedAnime(String animeId, String relatedAnimeId,
                                Anime.RelatedAnime.RelationType relationType, Integer seasonNumber) {
        log.info("Adding related anime {} to {}", relatedAnimeId, animeId);

        Anime anime = getAnimeById(animeId);
        Anime relatedAnime = getAnimeById(relatedAnimeId);

        // Check if relation already exists
        if (anime.getRelatedAnime() != null) {
            boolean exists = anime.getRelatedAnime().stream()
                    .anyMatch(r -> r.getAnimeId().equals(relatedAnimeId));
            if (exists) {
                throw new IllegalArgumentException("Связь с этим аниме уже существует");
            }
        }

        Anime.RelatedAnime relation = Anime.RelatedAnime.builder()
                .animeId(relatedAnimeId)
                .title(relatedAnime.getTitle())
                .posterUrl(relatedAnime.getPosterUrl())
                .relationType(relationType)
                .seasonNumber(seasonNumber)
                .build();

        if (anime.getRelatedAnime() == null) {
            anime.setRelatedAnime(new java.util.ArrayList<>());
        }
        anime.getRelatedAnime().add(relation);
        animeRepository.save(anime);
    }

    /**
     * Remove related anime
     */
    @Transactional
    public void removeRelatedAnime(String animeId, String relatedAnimeId) {
        log.info("Removing related anime {} from {}", relatedAnimeId, animeId);

        Anime anime = getAnimeById(animeId);

        if (anime.getRelatedAnime() != null) {
            anime.getRelatedAnime().removeIf(r -> r.getAnimeId().equals(relatedAnimeId));
            animeRepository.save(anime);
        }
    }

    /**
     * Get all related anime with full details
     */
    public List<Anime> getRelatedAnimeDetails(String animeId) {
        Anime anime = getAnimeById(animeId);

        if (anime.getRelatedAnime() == null || anime.getRelatedAnime().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<String> relatedIds = anime.getRelatedAnime().stream()
                .map(Anime.RelatedAnime::getAnimeId)
                .collect(java.util.stream.Collectors.toList());

        return animeRepository.findAllById(relatedIds)
                .stream()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Search anime by title (for autocomplete)
     */
    public List<Anime> searchByTitle(String query, int limit) {
        log.info("Searching anime by title: {}", query);
        Pageable pageable = PageRequest.of(0, limit);
        return animeRepository.findByTitleContainingIgnoreCase(query, pageable).getContent();
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }
}
