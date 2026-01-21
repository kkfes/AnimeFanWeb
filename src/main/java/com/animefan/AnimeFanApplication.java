package com.animefan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AnimeFan - Anime Catalog and Streaming Platform
 * Main Spring Boot Application class
 */
@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
@EnableAsync
public class AnimeFanApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeFanApplication.class, args);
    }
}
