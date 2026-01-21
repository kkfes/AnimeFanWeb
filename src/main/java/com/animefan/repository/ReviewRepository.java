package com.animefan.repository;

import com.animefan.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review entity
 */
@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find reviews by anime
    Page<Review> findByAnimeId(String animeId, Pageable pageable);

    List<Review> findByAnimeId(String animeId);

    // Find reviews by user
    Page<Review> findByUserId(String userId, Pageable pageable);

    List<Review> findByUserId(String userId);

    // Find specific review by user and anime
    Optional<Review> findByUserIdAndAnimeId(String userId, String animeId);

    // Check if user already reviewed anime
    boolean existsByUserIdAndAnimeId(String userId, String animeId);

    // Count reviews for anime
    long countByAnimeId(String animeId);

    // Count reviews by user
    long countByUserId(String userId);

    // Get average rating for anime
    @Aggregation(pipeline = {
            "{ $match: { animeId: ?0 } }",
            "{ $group: { _id: '$animeId', avgRating: { $avg: '$rating' }, count: { $sum: 1 } } }"
    })
    List<AnimeRatingStats> getAnimeRatingStats(String animeId);

    // Get rating distribution for anime
    @Aggregation(pipeline = {
            "{ $match: { animeId: ?0 } }",
            "{ $group: { _id: '$rating', count: { $sum: 1 } } }",
            "{ $sort: { _id: 1 } }"
    })
    List<RatingDistribution> getRatingDistribution(String animeId);

    // Get top reviewers
    @Aggregation(pipeline = {
            "{ $group: { _id: '$userId', username: { $first: '$username' }, reviewCount: { $sum: 1 } } }",
            "{ $sort: { reviewCount: -1 } }",
            "{ $limit: ?0 }"
    })
    List<TopReviewer> getTopReviewers(int limit);

    // Increment helpful count
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'helpfulCount': 1 } }")
    void incrementHelpfulCount(String reviewId);

    // Increment unhelpful count
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'unhelpfulCount': 1 } }")
    void incrementUnhelpfulCount(String reviewId);

    // Get recent reviews
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Get top helpful reviews for anime
    Page<Review> findByAnimeIdOrderByHelpfulCountDesc(String animeId, Pageable pageable);

    // Interface for anime rating statistics
    interface AnimeRatingStats {
        String getId();
        Double getAvgRating();
        Long getCount();
    }

    // Interface for rating distribution
    interface RatingDistribution {
        Integer getId();
        Long getCount();
    }

    // Interface for top reviewers
    interface TopReviewer {
        String getId();
        String getUsername();
        Long getReviewCount();
    }
}
