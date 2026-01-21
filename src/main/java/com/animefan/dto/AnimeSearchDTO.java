package com.animefan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for anime search requests with filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimeSearchDTO {

    private String query; // Full-text search query
    private List<String> genres; // Filter by genres
    private Integer yearFrom; // Filter by year range
    private Integer yearTo;
    private Double ratingFrom; // Filter by rating range
    private Double ratingTo;
    private String status; // ONGOING, COMPLETED, UPCOMING
    private String type; // TV, MOVIE, OVA, ONA, SPECIAL
    private String studioId;
    private String sortBy; // rating, releaseYear, title, viewCount, favoriteCount
    private String sortDirection; // asc, desc
    private Integer page = 0;
    private Integer size = 12;
}
