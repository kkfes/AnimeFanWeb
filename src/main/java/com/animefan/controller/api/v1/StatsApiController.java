package com.animefan.controller.api.v1;

import com.animefan.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for Statistics and Analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Platform statistics and analytics API")
public class StatsApiController {

    private final StatsService statsService;

    @GetMapping("/platform")
    @Operation(summary = "Get platform stats", description = "Get platform-wide statistics")
    public ResponseEntity<StatsService.PlatformStats> getPlatformStats() {
        log.info("GET /api/v1/stats/platform");
        StatsService.PlatformStats stats = statsService.getPlatformStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/genres")
    @Operation(summary = "Get genre stats", description = "Get statistics by genre")
    public ResponseEntity<?> getGenreStats() {
        log.info("GET /api/v1/stats/genres");
        return ResponseEntity.ok(statsService.getGenreStats());
    }

    @GetMapping("/anime/{animeId}")
    @Operation(summary = "Get anime stats", description = "Get statistics for specific anime")
    public ResponseEntity<StatsService.AnimeStats> getAnimeStats(@PathVariable String animeId) {
        log.info("GET /api/v1/stats/anime/{}", animeId);
        StatsService.AnimeStats stats = statsService.getAnimeStats(animeId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user stats", description = "Get statistics for specific user")
    public ResponseEntity<StatsService.UserStats> getUserStats(@PathVariable String userId) {
        log.info("GET /api/v1/stats/user/{}", userId);
        StatsService.UserStats stats = statsService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top/anime")
    @Operation(summary = "Get top anime", description = "Get top rated anime")
    public ResponseEntity<?> getTopAnime(@RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/stats/top/anime - limit: {}", limit);
        return ResponseEntity.ok(statsService.getTopAnimeByRating(limit));
    }

    @GetMapping("/top/reviewers")
    @Operation(summary = "Get top reviewers", description = "Get users with most reviews")
    public ResponseEntity<?> getTopReviewers(@RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/stats/top/reviewers - limit: {}", limit);
        return ResponseEntity.ok(statsService.getTopReviewers(limit));
    }
}
