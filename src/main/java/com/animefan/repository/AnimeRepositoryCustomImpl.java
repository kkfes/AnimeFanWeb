package com.animefan.repository;

import com.animefan.dto.AnimeSearchDTO;
import com.animefan.dto.GenreStatsDTO;
import com.animefan.model.Anime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Custom repository implementation for complex Anime queries using MongoTemplate
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AnimeRepositoryCustomImpl implements AnimeRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Anime> searchAnime(AnimeSearchDTO searchDTO) {
        log.debug("Searching anime with criteria: {}", searchDTO);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Full-text search if query is provided
        if (StringUtils.hasText(searchDTO.getQuery())) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(searchDTO.getQuery(), "i"),
                    Criteria.where("description").regex(searchDTO.getQuery(), "i"),
                    Criteria.where("titleEnglish").regex(searchDTO.getQuery(), "i"),
                    Criteria.where("titleJapanese").regex(searchDTO.getQuery(), "i")
            ));
        }

        // Genre filter
        if (searchDTO.getGenres() != null && !searchDTO.getGenres().isEmpty()) {
            criteriaList.add(Criteria.where("genres").in(searchDTO.getGenres()));
        }

        // Year range filter
        if (searchDTO.getYearFrom() != null && searchDTO.getYearTo() != null) {
            criteriaList.add(Criteria.where("releaseYear")
                    .gte(searchDTO.getYearFrom())
                    .lte(searchDTO.getYearTo()));
        } else if (searchDTO.getYearFrom() != null) {
            criteriaList.add(Criteria.where("releaseYear").gte(searchDTO.getYearFrom()));
        } else if (searchDTO.getYearTo() != null) {
            criteriaList.add(Criteria.where("releaseYear").lte(searchDTO.getYearTo()));
        }

        // Rating range filter
        if (searchDTO.getRatingFrom() != null) {
            criteriaList.add(Criteria.where("rating").gte(searchDTO.getRatingFrom()));
        }
        if (searchDTO.getRatingTo() != null) {
            criteriaList.add(Criteria.where("rating").lte(searchDTO.getRatingTo()));
        }

        // Status filter
        if (StringUtils.hasText(searchDTO.getStatus())) {
            criteriaList.add(Criteria.where("status").is(searchDTO.getStatus()));
        }

        // Type filter
        if (StringUtils.hasText(searchDTO.getType())) {
            criteriaList.add(Criteria.where("type").is(searchDTO.getType()));
        }

        // Studio filter
        if (StringUtils.hasText(searchDTO.getStudioId())) {
            criteriaList.add(Criteria.where("studioId").is(searchDTO.getStudioId()));
        }

        // Combine criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Sorting
        Sort sort = createSort(searchDTO.getSortBy(), searchDTO.getSortDirection());

        // Pagination
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 12;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get total count
        long total = mongoTemplate.count(query, Anime.class);

        // Apply pagination
        query.with(pageable);

        // Execute query
        List<Anime> animeList = mongoTemplate.find(query, Anime.class);

        log.debug("Found {} anime matching criteria", animeList.size());
        return new PageImpl<>(animeList, pageable, total);
    }

    @Override
    public Page<Anime> fullTextSearch(String queryText, int page, int size) {
        log.debug("Full-text search for: {}", queryText);

        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(queryText.split("\\s+"));

        Query query = TextQuery.queryText(textCriteria)
                .sortByScore()
                .with(PageRequest.of(page, size));

        long total = mongoTemplate.count(Query.query(Criteria.where("$text")
                .is(new org.bson.Document("$search", queryText))), Anime.class);

        List<Anime> results = mongoTemplate.find(query, Anime.class);

        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    @Override
    public List<GenreStatsDTO> getGenreStats() {
        log.debug("Getting genre statistics");

        Aggregation aggregation = newAggregation(
                unwind("genres"),
                group("genres")
                        .count().as("animeCount")
                        .avg("rating").as("averageRating")
                        .sum("viewCount").as("totalViews")
                        .sum("favoriteCount").as("totalFavorites"),
                project()
                        .and("_id").as("genre")
                        .and("animeCount").as("animeCount")
                        .and("averageRating").as("averageRating")
                        .and("totalViews").as("totalViews")
                        .and("totalFavorites").as("totalFavorites"),
                sort(Sort.Direction.DESC, "animeCount")
        );

        AggregationResults<GenreStatsDTO> results =
                mongoTemplate.aggregate(aggregation, "anime", GenreStatsDTO.class);

        return results.getMappedResults();
    }

    @Override
    public List<Anime> getTopAnimeByRating(int limit) {
        log.debug("Getting top {} anime by rating", limit);

        Aggregation aggregation = newAggregation(
                match(Criteria.where("ratingCount").gte(10)), // Minimum 10 ratings
                sort(Sort.Direction.DESC, "rating"),
                limit(limit)
        );

        AggregationResults<Anime> results =
                mongoTemplate.aggregate(aggregation, "anime", Anime.class);

        return results.getMappedResults();
    }

    @Override
    public void updateAnimeRating(String animeId, double newRating, int ratingCount) {
        log.debug("Updating rating for anime {}: {} ({} ratings)", animeId, newRating, ratingCount);

        Query query = Query.query(Criteria.where("id").is(animeId));
        Update update = new Update()
                .set("rating", newRating)
                .set("ratingCount", ratingCount);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    @Override
    public void incrementViewCount(String animeId) {
        log.debug("Incrementing view count for anime: {}", animeId);

        Query query = Query.query(Criteria.where("id").is(animeId));
        Update update = new Update().inc("viewCount", 1);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    @Override
    public void updateFavoriteCount(String animeId, int delta) {
        log.debug("Updating favorite count for anime {} by {}", animeId, delta);

        Query query = Query.query(Criteria.where("id").is(animeId));
        Update update = new Update().inc("favoriteCount", delta);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    @Override
    public void addEpisode(String animeId, Anime.Episode episode) {
        log.debug("Adding episode {} to anime: {}", episode.getNumber(), animeId);

        Query query = Query.query(Criteria.where("id").is(animeId));
        Update update = new Update()
                .push("episodes", episode)
                .inc("episodeCount", 1);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    @Override
    public void updateEpisode(String animeId, int episodeNumber, Anime.Episode episode) {
        log.debug("Updating episode {} in anime: {}", episodeNumber, animeId);

        Query query = Query.query(Criteria.where("id").is(animeId)
                .and("episodes.number").is(episodeNumber));
        Update update = new Update()
                .set("episodes.$", episode);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    @Override
    public void removeEpisode(String animeId, int episodeNumber) {
        log.debug("Removing episode {} from anime: {}", episodeNumber, animeId);

        Query query = Query.query(Criteria.where("id").is(animeId));
        Update update = new Update()
                .pull("episodes", Query.query(Criteria.where("number").is(episodeNumber)))
                .inc("episodeCount", -1);

        mongoTemplate.updateFirst(query, update, Anime.class);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (!StringUtils.hasText(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return switch (sortBy.toLowerCase()) {
            case "title" -> Sort.by(direction, "title");
            case "releaseyear", "year" -> Sort.by(direction, "releaseYear");
            case "viewcount", "views" -> Sort.by(direction, "viewCount");
            case "favoritecount", "favorites" -> Sort.by(direction, "favoriteCount");
            case "createdat", "new" -> Sort.by(direction, "createdAt");
            default -> Sort.by(direction, "rating");
        };
    }
}
