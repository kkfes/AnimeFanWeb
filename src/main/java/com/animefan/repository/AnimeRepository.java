package com.animefan.repository;

import com.animefan.model.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Anime entity with custom queries and aggregations
 */
@Repository
public interface AnimeRepository extends MongoRepository<Anime, String>, AnimeRepositoryCustom {

    // Basic queries
    Page<Anime> findByGenresContaining(String genre, Pageable pageable);

    Page<Anime> findByStatus(String status, Pageable pageable);

    Page<Anime> findByType(String type, Pageable pageable);

    Page<Anime> findByStudioId(String studioId, Pageable pageable);

    Page<Anime> findByReleaseYear(Integer year, Pageable pageable);

    Page<Anime> findByReleaseYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    // Rating queries
    Page<Anime> findByRatingGreaterThanEqual(Double rating, Pageable pageable);

    List<Anime> findTop10ByOrderByRatingDesc();

    List<Anime> findTop10ByOrderByViewCountDesc();

    List<Anime> findTop10ByOrderByFavoriteCountDesc();

    // Full-text search using MongoDB text index
    @Query("{ $text: { $search: ?0 } }")
    Page<Anime> searchByText(String text, Pageable pageable);

    // Search with text score
    @Query(value = "{ $text: { $search: ?0 } }", fields = "{ score: { $meta: 'textScore' } }")
    Page<Anime> searchByTextWithScore(String text, Pageable pageable);

    // Complex search with multiple criteria
    @Query("{ $and: [ " +
            "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }, " +
            "{ 'genres': { $in: ?1 } }, " +
            "{ 'releaseYear': { $gte: ?2, $lte: ?3 } }, " +
            "{ 'rating': { $gte: ?4 } } " +
            "] }")
    Page<Anime> findByComplexCriteria(String query, List<String> genres,
                                       Integer yearFrom, Integer yearTo,
                                       Double minRating, Pageable pageable);

    // Get all distinct genres
    @Aggregation(pipeline = {
            "{ $unwind: '$genres' }",
            "{ $group: { _id: '$genres' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findAllGenres();

    // Get anime count by genre
    @Aggregation(pipeline = {
            "{ $unwind: '$genres' }",
            "{ $group: { _id: '$genres', count: { $sum: 1 }, avgRating: { $avg: '$rating' } } }",
            "{ $sort: { count: -1 } }"
    })
    List<GenreCount> getGenreStatistics();

    // Get anime by studio with statistics
    @Aggregation(pipeline = {
            "{ $match: { studioId: ?0 } }",
            "{ $group: { _id: '$studioId', " +
                    "animeCount: { $sum: 1 }, " +
                    "avgRating: { $avg: '$rating' }, " +
                    "totalViews: { $sum: '$viewCount' } } }"
    })
    StudioAnimeStats getStudioStatistics(String studioId);

    // Check if anime exists by title
    boolean existsByTitle(String title);

    // Search by title (for autocomplete)
    Page<Anime> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Count by genre
    long countByGenresContaining(String genre);

    // Count by status
    long countByStatus(String status);

    // Interface for genre statistics aggregation result
    interface GenreCount {
        String getId();
        Long getCount();
        Double getAvgRating();
    }

    // Interface for studio statistics aggregation result
    interface StudioAnimeStats {
        String getId();
        Long getAnimeCount();
        Double getAvgRating();
        Long getTotalViews();
    }
}
