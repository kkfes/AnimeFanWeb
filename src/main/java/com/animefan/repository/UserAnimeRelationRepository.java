package com.animefan.repository;

import com.animefan.model.UserAnimeRelation;
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
 * Repository for UserAnimeRelation entity
 */
@Repository
public interface UserAnimeRelationRepository extends MongoRepository<UserAnimeRelation, String> {

    // Find by user and anime
    Optional<UserAnimeRelation> findByUserIdAndAnimeId(String userId, String animeId);

    // Check if relation exists
    boolean existsByUserIdAndAnimeId(String userId, String animeId);

    // Find all relations for user
    Page<UserAnimeRelation> findByUserId(String userId, Pageable pageable);

    List<UserAnimeRelation> findByUserId(String userId);

    // Find relations by user and status
    Page<UserAnimeRelation> findByUserIdAndStatus(String userId, UserAnimeRelation.Status status, Pageable pageable);

    List<UserAnimeRelation> findByUserIdAndStatus(String userId, UserAnimeRelation.Status status);

    // Find user's favorites
    Page<UserAnimeRelation> findByUserIdAndFavoriteTrue(String userId, Pageable pageable);

    List<UserAnimeRelation> findByUserIdAndFavoriteTrue(String userId);

    // Count by user and status
    long countByUserIdAndStatus(String userId, UserAnimeRelation.Status status);

    // Count favorites for user
    long countByUserIdAndFavoriteTrue(String userId);

    // Count users for anime
    long countByAnimeId(String animeId);

    // Count users with anime in favorites
    long countByAnimeIdAndFavoriteTrue(String animeId);

    // Update progress
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'episodesWatched': ?1 } }")
    void updateEpisodesWatched(String relationId, int episodesWatched);

    // Update status
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'status': ?1 } }")
    void updateStatus(String relationId, UserAnimeRelation.Status status);

    // Toggle favorite
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'favorite': ?1 } }")
    void updateFavorite(String relationId, boolean favorite);

    // Get user statistics by status
    @Aggregation(pipeline = {
            "{ $match: { userId: ?0 } }",
            "{ $group: { _id: '$status', count: { $sum: 1 } } }"
    })
    List<StatusCount> getUserStatusCounts(String userId);

    // Get genre preferences for user
    @Aggregation(pipeline = {
            "{ $match: { userId: ?0, status: 'COMPLETED' } }",
            "{ $lookup: { from: 'anime', localField: 'animeId', foreignField: '_id', as: 'anime' } }",
            "{ $unwind: '$anime' }",
            "{ $unwind: '$anime.genres' }",
            "{ $group: { _id: '$anime.genres', count: { $sum: 1 }, avgRating: { $avg: '$userRating' } } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 10 }"
    })
    List<GenrePreference> getUserGenrePreferences(String userId);

    // Delete all relations for user
    void deleteByUserId(String userId);

    // Delete all relations for anime
    void deleteByAnimeId(String animeId);

    // Interface for status count
    interface StatusCount {
        String getId();
        Long getCount();
    }

    // Interface for genre preference
    interface GenrePreference {
        String getId();
        Long getCount();
        Double getAvgRating();
    }
}
