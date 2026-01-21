package com.animefan.config;

import com.animefan.model.Anime;
import com.animefan.model.Review;
import com.animefan.model.User;
import com.animefan.model.UserAnimeRelation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * MongoDB Configuration
 * Note: Transactions are disabled because MongoDB standalone does not support them.
 * To enable transactions, use MongoDB replica set.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mongoMappingContext;


    /**
     * Initialize indexes on application startup
     */
    @PostConstruct
    public void initIndexes() {
        log.info("Initializing MongoDB indexes...");

        // Create indexes from annotations
        createIndexesFor(Anime.class);
        createIndexesFor(User.class);
        createIndexesFor(Review.class);
        createIndexesFor(UserAnimeRelation.class);

        // Create text index for full-text search on Anime
        createTextIndex();

        log.info("MongoDB indexes initialized successfully");
    }

    private void createIndexesFor(Class<?> entityClass) {
        try {
            IndexOperations indexOps = mongoTemplate.indexOps(entityClass);
            IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
            resolver.resolveIndexFor(entityClass).forEach(indexOps::ensureIndex);
            log.debug("Created indexes for: {}", entityClass.getSimpleName());
        } catch (Exception e) {
            log.warn("Failed to create indexes for {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
    }

    private void createTextIndex() {
        try {
            // Text index is created via @TextIndexed annotations on Anime entity
            log.info("Text index for full-text search is configured via annotations");
        } catch (Exception e) {
            log.warn("Failed to create text index: {}", e.getMessage());
        }
    }
}
