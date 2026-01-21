package com.animefan.service;

import com.animefan.dto.ReviewDTO;
import com.animefan.exception.ResourceNotFoundException;
import com.animefan.exception.ValidationException;
import com.animefan.model.Anime;
import com.animefan.model.Review;
import com.animefan.model.User;
import com.animefan.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Review business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AnimeService animeService;
    private final UserService userService;

    /**
     * Get all reviews with pagination
     */
    public Page<Review> getAllReviews(int page, int size) {
        log.info("Getting all reviews, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageable);
    }

    /**
     * Get review by ID
     */
    public Review getReviewById(String id) {
        log.info("Getting review by ID: {}", id);
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    /**
     * Get reviews for anime
     */
    public Page<Review> getReviewsForAnime(String animeId, int page, int size) {
        log.info("Getting reviews for anime: {}", animeId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByAnimeId(animeId, pageable);
    }

    /**
     * Get reviews by user
     */
    public Page<Review> getReviewsByUser(String userId, int page, int size) {
        log.info("Getting reviews by user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByUserId(userId, pageable);
    }

    /**
     * Get top helpful reviews for anime
     */
    public Page<Review> getTopReviewsForAnime(String animeId, int page, int size) {
        log.info("Getting top reviews for anime: {}", animeId);
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByAnimeIdOrderByHelpfulCountDesc(animeId, pageable);
    }

    /**
     * Create new review
     */
    @Transactional
    public Review createReview(String userId, ReviewDTO reviewDTO) {
        log.info("Creating review for anime {} by user {}", reviewDTO.getAnimeId(), userId);

        // Check if user already reviewed this anime
        if (reviewRepository.existsByUserIdAndAnimeId(userId, reviewDTO.getAnimeId())) {
            throw new ValidationException("You have already reviewed this anime");
        }

        // Get user and anime details
        User user = userService.getUserById(userId);
        Anime anime = animeService.getAnimeById(reviewDTO.getAnimeId());

        Review review = Review.builder()
                .userId(userId)
                .username(user.getUsername())
                .userAvatarUrl(user.getAvatarUrl())
                .animeId(anime.getId())
                .animeTitle(anime.getTitle())
                .rating(reviewDTO.getRating())
                .title(reviewDTO.getTitle())
                .text(reviewDTO.getText())
                .spoiler(reviewDTO.getSpoiler() != null ? reviewDTO.getSpoiler() : false)
                .helpfulCount(0)
                .unhelpfulCount(0)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update user review count
        userService.incrementReviewCount(userId, 1);

        // Recalculate anime rating
        animeService.recalculateRating(anime.getId());

        return savedReview;
    }

    /**
     * Update review
     */
    @Transactional
    public Review updateReview(String reviewId, String userId, ReviewDTO reviewDTO) {
        log.info("Updating review: {}", reviewId);

        Review existingReview = getReviewById(reviewId);

        // Verify ownership
        if (!existingReview.getUserId().equals(userId)) {
            throw new ValidationException("You can only edit your own reviews");
        }

        // Update fields
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setTitle(reviewDTO.getTitle());
        existingReview.setText(reviewDTO.getText());
        existingReview.setSpoiler(reviewDTO.getSpoiler() != null ? reviewDTO.getSpoiler() : false);

        Review updatedReview = reviewRepository.save(existingReview);

        // Recalculate anime rating
        animeService.recalculateRating(existingReview.getAnimeId());

        return updatedReview;
    }

    /**
     * Delete review (admin version - no ownership check)
     */
    public void deleteReview(String reviewId) {
        log.info("Admin deleting review: {}", reviewId);

        Review review = getReviewById(reviewId);

        String animeId = review.getAnimeId();
        String reviewUserId = review.getUserId();

        reviewRepository.deleteById(reviewId);

        // Update user review count
        userService.incrementReviewCount(reviewUserId, -1);

        // Recalculate anime rating
        animeService.recalculateRating(animeId);
    }

    /**
     * Delete review
     */
    public void deleteReview(String reviewId, String userId, boolean isAdmin) {
        log.info("Deleting review: {}", reviewId);

        Review review = getReviewById(reviewId);

        // Verify ownership or admin rights
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new ValidationException("You can only delete your own reviews");
        }

        String animeId = review.getAnimeId();
        String reviewUserId = review.getUserId();

        reviewRepository.deleteById(reviewId);

        // Update user review count
        userService.incrementReviewCount(reviewUserId, -1);

        // Recalculate anime rating
        animeService.recalculateRating(animeId);
    }

    /**
     * Mark review as helpful
     */
    public void markHelpful(String reviewId) {
        log.info("Marking review as helpful: {}", reviewId);
        reviewRepository.incrementHelpfulCount(reviewId);
    }

    /**
     * Mark review as unhelpful
     */
    @Transactional
    public void markUnhelpful(String reviewId) {
        log.info("Marking review as unhelpful: {}", reviewId);
        reviewRepository.incrementUnhelpfulCount(reviewId);
    }

    /**
     * Get user's review for specific anime
     */
    public Review getUserReviewForAnime(String userId, String animeId) {
        return reviewRepository.findByUserIdAndAnimeId(userId, animeId).orElse(null);
    }

    /**
     * Check if user has reviewed anime
     */
    public boolean hasUserReviewedAnime(String userId, String animeId) {
        return reviewRepository.existsByUserIdAndAnimeId(userId, animeId);
    }

    /**
     * Get review count for anime
     */
    public long getReviewCountForAnime(String animeId) {
        return reviewRepository.countByAnimeId(animeId);
    }

    /**
     * Get review count for user
     */
    public long getReviewCountForUser(String userId) {
        return reviewRepository.countByUserId(userId);
    }

    /**
     * Get rating distribution for anime
     */
    public List<ReviewRepository.RatingDistribution> getRatingDistribution(String animeId) {
        try {
            return reviewRepository.getRatingDistribution(animeId);
        } catch (Exception e) {
            log.warn("Failed to get rating distribution: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Get top reviewers
     */
    public List<ReviewRepository.TopReviewer> getTopReviewers(int limit) {
        try {
            return reviewRepository.getTopReviewers(limit);
        } catch (Exception e) {
            log.warn("Failed to get top reviewers: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Get recent reviews
     */
    public Page<Review> getRecentReviews(int page, int size) {
        log.info("Getting recent reviews");
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
