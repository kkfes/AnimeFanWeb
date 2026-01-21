package com.animefan.repository;

import com.animefan.model.Studio;
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
 * Repository for Studio entity
 */
@Repository
public interface StudioRepository extends MongoRepository<Studio, String> {

    Optional<Studio> findByName(String name);

    boolean existsByName(String name);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Studio> searchByName(String query, Pageable pageable);

    Page<Studio> findByCountry(String country, Pageable pageable);

    List<Studio> findByFoundedYearBetween(Integer startYear, Integer endYear);

    // Get all distinct countries
    @Aggregation(pipeline = {
            "{ $group: { _id: '$country' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findAllCountries();

    // Update anime count for studio
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'animeCount': ?1 } }")
    void updateAnimeCount(String studioId, int delta);

    // Add anime ID to studio's list
    @Query("{ '_id': ?0 }")
    @Update("{ '$push': { 'animeIds': ?1 }, '$inc': { 'animeCount': 1 } }")
    void addAnimeToStudio(String studioId, String animeId);

    // Remove anime ID from studio's list
    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'animeIds': ?1 }, '$inc': { 'animeCount': -1 } }")
    void removeAnimeFromStudio(String studioId, String animeId);

    // Get studios with most anime
    @Aggregation(pipeline = {
            "{ $sort: { animeCount: -1 } }",
            "{ $limit: ?0 }"
    })
    List<Studio> findTopStudios(int limit);
}
