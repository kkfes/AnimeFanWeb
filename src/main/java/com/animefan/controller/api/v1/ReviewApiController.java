package com.animefan.controller.api.v1;

import com.animefan.dto.ReviewDTO;
import com.animefan.model.Review;
import com.animefan.model.User;
import com.animefan.repository.ReviewRepository;
import com.animefan.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Review operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management API")
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Get paginated list of all reviews")
    public ResponseEntity<Page<ReviewDTO>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/reviews - page: {}, size: {}", page, size);
        Page<Review> reviews = reviewService.getAllReviews(page, size);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::fromEntity);
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Get review details by ID")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable String id) {
        log.info("GET /api/v1/reviews/{}", id);
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ReviewDTO.fromEntity(review));
    }

    @GetMapping("/anime/{animeId}")
    @Operation(summary = "Get reviews for anime", description = "Get all reviews for specific anime")
    public ResponseEntity<Page<ReviewDTO>> getReviewsForAnime(
            @PathVariable String animeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/reviews/anime/{}", animeId);
        Page<Review> reviews = reviewService.getReviewsForAnime(animeId, page, size);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::fromEntity);
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/anime/{animeId}/top")
    @Operation(summary = "Get top reviews for anime", description = "Get most helpful reviews for anime")
    public ResponseEntity<Page<ReviewDTO>> getTopReviewsForAnime(
            @PathVariable String animeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.info("GET /api/v1/reviews/anime/{}/top", animeId);
        Page<Review> reviews = reviewService.getTopReviewsForAnime(animeId, page, size);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::fromEntity);
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user", description = "Get all reviews by specific user")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/reviews/user/{}", userId);
        Page<Review> reviews = reviewService.getReviewsByUser(userId, page, size);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::fromEntity);
        return ResponseEntity.ok(reviewDTOs);
    }

    @PostMapping
    @Operation(summary = "Create review", description = "Create new review for anime")
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        log.info("POST /api/v1/reviews - animeId: {}", reviewDTO.getAnimeId());
        Review review = reviewService.createReview(user.getId(), reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewDTO.fromEntity(review));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Update existing review")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable String id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        log.info("PUT /api/v1/reviews/{}", id);
        Review review = reviewService.updateReview(id, user.getId(), reviewDTO);
        return ResponseEntity.ok(ReviewDTO.fromEntity(review));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Delete review")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {

        log.info("DELETE /api/v1/reviews/{}", id);
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(id, user.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Mark as helpful", description = "Mark review as helpful")
    public ResponseEntity<Void> markHelpful(@PathVariable String id) {
        log.info("POST /api/v1/reviews/{}/helpful", id);
        reviewService.markHelpful(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unhelpful")
    @Operation(summary = "Mark as unhelpful", description = "Mark review as unhelpful")
    public ResponseEntity<Void> markUnhelpful(@PathVariable String id) {
        log.info("POST /api/v1/reviews/{}/unhelpful", id);
        reviewService.markUnhelpful(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/anime/{animeId}/distribution")
    @Operation(summary = "Get rating distribution", description = "Get rating distribution for anime")
    public ResponseEntity<List<ReviewRepository.RatingDistribution>> getRatingDistribution(
            @PathVariable String animeId) {

        log.info("GET /api/v1/reviews/anime/{}/distribution", animeId);
        List<ReviewRepository.RatingDistribution> distribution = reviewService.getRatingDistribution(animeId);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent reviews", description = "Get most recent reviews")
    public ResponseEntity<Page<ReviewDTO>> getRecentReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/v1/reviews/recent");
        Page<Review> reviews = reviewService.getRecentReviews(page, size);
        Page<ReviewDTO> reviewDTOs = reviews.map(ReviewDTO::fromEntity);
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/top-reviewers")
    @Operation(summary = "Get top reviewers", description = "Get users with most reviews")
    public ResponseEntity<List<ReviewRepository.TopReviewer>> getTopReviewers(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/v1/reviews/top-reviewers");
        List<ReviewRepository.TopReviewer> topReviewers = reviewService.getTopReviewers(limit);
        return ResponseEntity.ok(topReviewers);
    }
}
