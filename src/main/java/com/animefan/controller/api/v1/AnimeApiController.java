package com.animefan.controller.api.v1;

import com.animefan.dto.AnimeDTO;
import com.animefan.dto.AnimeSearchDTO;
import com.animefan.dto.GenreStatsDTO;
import com.animefan.model.Anime;
import com.animefan.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Anime operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/anime")
@RequiredArgsConstructor
@Tag(name = "Anime", description = "Anime management API")
public class AnimeApiController {

    private final AnimeService animeService;

    @GetMapping
    @Operation(summary = "Get all anime", description = "Get paginated list of all anime")
    public ResponseEntity<Page<Anime>> getAllAnime(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "rating") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("GET /api/v1/anime - page: {}, size: {}", page, size);
        Page<Anime> animePage = animeService.getAllAnime(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(animePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get anime by ID", description = "Get anime details by ID")
    public ResponseEntity<Anime> getAnimeById(
            @Parameter(description = "Anime ID") @PathVariable String id,
            @Parameter(description = "Increment view count") @RequestParam(defaultValue = "false") boolean view) {

        log.info("GET /api/v1/anime/{}", id);
        Anime anime = view ? animeService.getAnimeByIdWithView(id) : animeService.getAnimeById(id);
        return ResponseEntity.ok(anime);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create anime", description = "Create new anime (Admin only)")
    public ResponseEntity<Anime> createAnime(@Valid @RequestBody AnimeDTO animeDTO) {
        log.info("POST /api/v1/anime - title: {}", animeDTO.getTitle());
        Anime anime = animeService.createAnime(animeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(anime);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update anime", description = "Update existing anime (Admin only)")
    public ResponseEntity<Anime> updateAnime(
            @Parameter(description = "Anime ID") @PathVariable String id,
            @Valid @RequestBody AnimeDTO animeDTO) {

        log.info("PUT /api/v1/anime/{}", id);
        Anime anime = animeService.updateAnime(id, animeDTO);
        return ResponseEntity.ok(anime);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete anime", description = "Delete anime (Admin only)")
    public ResponseEntity<Void> deleteAnime(
            @Parameter(description = "Anime ID") @PathVariable String id) {

        log.info("DELETE /api/v1/anime/{}", id);
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search anime", description = "Full-text search by title and description")
    public ResponseEntity<Page<Anime>> searchAnime(
            @Parameter(description = "Search query") @RequestParam String q,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "12") int size) {

        log.info("GET /api/v1/anime/search - query: {}", q);
        Page<Anime> results = animeService.fullTextSearch(q, page, size);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/search/advanced")
    @Operation(summary = "Advanced search", description = "Search with multiple filters")
    public ResponseEntity<Page<Anime>> advancedSearch(@RequestBody AnimeSearchDTO searchDTO) {
        log.info("POST /api/v1/anime/search/advanced - {}", searchDTO);
        Page<Anime> results = animeService.searchAnime(searchDTO);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/genre/{genre}")
    @Operation(summary = "Get anime by genre", description = "Get anime filtered by genre")
    public ResponseEntity<Page<Anime>> getAnimeByGenre(
            @Parameter(description = "Genre name") @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("GET /api/v1/anime/genre/{}", genre);
        Page<Anime> animePage = animeService.getAnimeByGenre(genre, page, size);
        return ResponseEntity.ok(animePage);
    }

    @GetMapping("/studio/{studioId}")
    @Operation(summary = "Get anime by studio", description = "Get anime from specific studio")
    public ResponseEntity<Page<Anime>> getAnimeByStudio(
            @Parameter(description = "Studio ID") @PathVariable String studioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("GET /api/v1/anime/studio/{}", studioId);
        Page<Anime> animePage = animeService.getAnimeByStudio(studioId, page, size);
        return ResponseEntity.ok(animePage);
    }

    @GetMapping("/top")
    @Operation(summary = "Get top anime", description = "Get top rated anime")
    public ResponseEntity<List<Anime>> getTopAnime(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/v1/anime/top - limit: {}", limit);
        List<Anime> topAnime = animeService.getTopAnime(limit);
        return ResponseEntity.ok(topAnime);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent anime", description = "Get recently added anime")
    public ResponseEntity<Page<Anime>> getRecentAnime(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("GET /api/v1/anime/recent");
        Page<Anime> animePage = animeService.getRecentAnime(page, size);
        return ResponseEntity.ok(animePage);
    }

    @GetMapping("/genres")
    @Operation(summary = "Get all genres", description = "Get list of all genres")
    public ResponseEntity<List<String>> getAllGenres() {
        log.info("GET /api/v1/anime/genres");
        List<String> genres = animeService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/genres/stats")
    @Operation(summary = "Get genre statistics", description = "Get aggregated statistics by genre")
    public ResponseEntity<List<GenreStatsDTO>> getGenreStatistics() {
        log.info("GET /api/v1/anime/genres/stats");
        List<GenreStatsDTO> stats = animeService.getGenreStatistics();
        return ResponseEntity.ok(stats);
    }

    // Episode management endpoints

    @PostMapping("/{animeId}/episodes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add episode", description = "Add episode to anime (Admin only)")
    public ResponseEntity<Void> addEpisode(
            @PathVariable String animeId,
            @Valid @RequestBody Anime.Episode episode) {

        log.info("POST /api/v1/anime/{}/episodes - number: {}", animeId, episode.getNumber());
        animeService.addEpisode(animeId, episode);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{animeId}/episodes/{episodeNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update episode", description = "Update specific episode (Admin only)")
    public ResponseEntity<Void> updateEpisode(
            @PathVariable String animeId,
            @PathVariable int episodeNumber,
            @Valid @RequestBody Anime.Episode episode) {

        log.info("PUT /api/v1/anime/{}/episodes/{}", animeId, episodeNumber);
        animeService.updateEpisode(animeId, episodeNumber, episode.getTitle(), episode.getVideoUrl(), episode.getThumbnailUrl(), episode.getDuration());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{animeId}/episodes/{episodeNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete episode", description = "Delete episode from anime (Admin only)")
    public ResponseEntity<Void> deleteEpisode(
            @PathVariable String animeId,
            @PathVariable int episodeNumber) {

        log.info("DELETE /api/v1/anime/{}/episodes/{}", animeId, episodeNumber);
        animeService.deleteEpisode(animeId, episodeNumber);
        return ResponseEntity.noContent().build();
    }
}
